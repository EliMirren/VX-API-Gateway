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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import freemarker.cache.NullCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystemException;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import io.vertx.ext.web.templ.freemarker.impl.VertxWebObjectWrapper;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class FreeMarkerTemplateHander extends CachingTemplateEngine<Template> implements FreeMarkerTemplateEngine, TemplateHandler {
	private static final Logger LOG = LogManager.getLogger(FreeMarkerTemplateHander.class);

	private final Configuration config;
	private String contentType;

	public FreeMarkerTemplateHander(Vertx vertx, String templateDirectory, String contentType) {
		super(DEFAULT_TEMPLATE_EXTENSION, DEFAULT_MAX_CACHE_SIZE);
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
	public FreeMarkerTemplateEngine setExtension(String extension) {
		doSetExtension(extension);
		return this;
	}

	@Override
	public FreeMarkerTemplateEngine setMaxCacheSize(int maxCacheSize) {
		this.cache.setMaxSize(maxCacheSize);
		return this;
	}

	@Override
	public void render(Map<String, Object> context, String templateFile, Handler<AsyncResult<Buffer>> handler) {
		try {
			Template template = isCachingEnabled() ? cache.get(templateFile) : null;
			if (template == null) {
				synchronized (this) {
					template = config.getTemplate(adjustLocation(templateFile));
				}
				if (isCachingEnabled()) {
					cache.put(templateFile, template);
				}
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
}
