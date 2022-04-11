/*
 * Copyright 2016 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package com.szmirren.vxApi.core.handler;

import freemarker.cache.NullCacheStorage;
import freemarker.template.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystemException;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.WebEnvironment;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import io.vertx.ext.web.common.template.impl.TemplateHolder;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class FreeMarkerTemplateHander extends CachingTemplateEngine<Template> implements FreeMarkerTemplateEngine, TemplateHandler {
  private static final Logger LOG = LogManager.getLogger(FreeMarkerTemplateHander.class);
  private final LocalMap<String, TemplateHolder<Template>> cache;
  private final Configuration config;
  private final String contentType;

  public FreeMarkerTemplateHander(Vertx vertx, String templateDirectory, String contentType) {
    super(vertx, DEFAULT_TEMPLATE_EXTENSION);
    if (!WebEnvironment.development()) {
      this.cache = vertx.sharedData().getLocalMap("__vx.api.vertx.web.template.cache");
    } else {
      this.cache = null;
    }
    this.contentType = contentType;
    config = new Configuration(Configuration.VERSION_2_3_28);
    try {
      config.setDirectoryForTemplateLoading(new File(templateDirectory));
    } catch (IOException e) {
      throw new FileSystemException("not found template directory:" + templateDirectory);
    }
    config.setObjectWrapper(new VertxWebObjectWrapper(config.getIncompatibleImprovements()));
    config.setCacheStorage(new NullCacheStorage());
  }

  @Override
  public void render(Map<String, Object> context, String templateFile, Handler<AsyncResult<Buffer>> handler) {
    try {
      TemplateHolder<Template> holder = this.cache.get(templateFile);
      Template template =  holder==null?null:holder.template();
      if (template == null) {
        synchronized (this) {
          template = config.getTemplate(adjustLocation(templateFile));
        }
        this.cache.put(templateFile, new TemplateHolder<>(template));
      }
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        template.process(context, new OutputStreamWriter(baos));
        handler.handle(Future.succeededFuture(Buffer.buffer(baos.toByteArray())));
      }
    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }

  @Override
  public void clearCache() {
    this.cache.clear();
  }

  @Override
  public void handle(RoutingContext context) {
    String file = Utils.pathOffset(context.normalisedPath(), context);
    if (file != null && file.startsWith("/")) {
      file = file.substring(1);
    }
    render(new JsonObject(context.data()), file, res -> {
      if (res.succeeded()) {
        context.response().putHeader(HttpHeaders.CONTENT_TYPE, contentType).end(res.result());
      } else {
        context.fail(res.cause());
      }
    });

  }

  @Override
  public TemplateHandler setIndexTemplate(String indexTemplate) {
    return this;
  }

  class VertxWebObjectWrapper extends DefaultObjectWrapper {
    public VertxWebObjectWrapper(Version incompatibleImprovements) {
      super(incompatibleImprovements);
    }

    protected TemplateModel handleUnknownType(Object obj) throws TemplateModelException {
      if (obj instanceof JsonArray) {
        return new JsonArrayAdapter((JsonArray) obj, this);
      } else {
        return (TemplateModel) (obj instanceof JsonObject ? new JsonObjectAdapter((JsonObject) obj, this) : super.handleUnknownType(obj));
      }
    }
  }

  class JsonArrayAdapter extends WrappingTemplateModel implements TemplateSequenceModel, AdapterTemplateModel {
    private final JsonArray jsonArray;

    public JsonArrayAdapter(JsonArray jsonArray, ObjectWrapper ow) {
      super(ow);
      this.jsonArray = jsonArray;
    }

    public int size() {
      return this.jsonArray.size();
    }

    public TemplateModel get(int index) throws TemplateModelException {
      return index >= 0 && index < this.jsonArray.size() ? this.wrap(this.jsonArray.getValue(index)) : null;
    }

    public Object getAdaptedObject(Class hint) {
      return this.jsonArray;
    }
  }

  class JsonObjectAdapter extends WrappingTemplateModel implements TemplateHashModelEx, AdapterTemplateModel {
    private final JsonObject jsonObject;

    public JsonObjectAdapter(JsonObject jsonObject, ObjectWrapper ow) {
      super(ow);
      this.jsonObject = jsonObject;
    }

    public TemplateModel get(String key) throws TemplateModelException {
      Object value = this.jsonObject.getValue(key);
      return value == null ? null : this.wrap(value);
    }

    public boolean isEmpty() {
      return this.jsonObject.isEmpty();
    }

    public int size() {
      return this.jsonObject.size();
    }

    public TemplateCollectionModel keys() {
      return new SimpleCollection(this.jsonObject.fieldNames(), this.getObjectWrapper());
    }

    public TemplateCollectionModel values() {
      return new SimpleCollection(this.jsonObject.getMap().values(), this.getObjectWrapper());
    }

    public Object getAdaptedObject(Class hint) {
      return this.jsonObject;
    }
  }
}
