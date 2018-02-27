package com.szmirren.vxApi.core.handler.freemarker.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import com.szmirren.vxApi.core.handler.freemarker.VxApiFreeMarkerTemplateEngine;

import freemarker.cache.NullCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystemException;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;
import io.vertx.ext.web.templ.impl.CachingTemplateEngine;
import io.vertx.ext.web.templ.impl.VertxWebObjectWrapper;

public class VxApiFreeMarkerTemplateEngineImpl extends CachingTemplateEngine<Template>
		implements VxApiFreeMarkerTemplateEngine {
	private final Configuration config;

	public VxApiFreeMarkerTemplateEngineImpl() {
		this("templates");
	}

	public VxApiFreeMarkerTemplateEngineImpl(String templateDirectory) {
		super(DEFAULT_TEMPLATE_EXTENSION, DEFAULT_MAX_CACHE_SIZE);
		config = new Configuration(Configuration.VERSION_2_3_23);
		try {
			config.setDirectoryForTemplateLoading(new File(templateDirectory));
		} catch (IOException e) {
			throw new FileSystemException("not found template directory:" + templateDirectory);
		}
		config.setObjectWrapper(new VertxWebObjectWrapper(config.getIncompatibleImprovements()));
		config.setCacheStorage(new NullCacheStorage());
		config.setDefaultEncoding("UTF-8");
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
	public void render(RoutingContext context, String templateDirectory, String templateFileName,
			Handler<AsyncResult<Buffer>> handler) {
		context.vertx().<AsyncResult<Template>>executeBlocking(exec -> {
			String fileName = templateDirectory + templateFileName;
			Template template = isCachingEnabled() ? cache.get(fileName) : null;
			if (template == null) {
				try {
					template = config.getTemplate(templateFileName);
					if (isCachingEnabled()) {
						cache.put(fileName, template);
					}
				} catch (IOException e) {
					exec.fail(e);
					return;
				}
			}
			exec.complete(Future.succeededFuture(template));
		}, res -> {
			if (res.succeeded()) {
				try {
					Template template = res.result().result();
					Map<String, RoutingContext> variables = new HashMap<>(1);
					variables.put("context", context);
					try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
						template.process(variables, new OutputStreamWriter(baos));
						handler.handle(Future.succeededFuture(Buffer.buffer(baos.toByteArray())));
					}
				} catch (Exception e) {
					handler.handle(Future.failedFuture(e));
				}
			} else {
				handler.handle(Future.failedFuture(res.cause()));
			}
		});

	}

}
