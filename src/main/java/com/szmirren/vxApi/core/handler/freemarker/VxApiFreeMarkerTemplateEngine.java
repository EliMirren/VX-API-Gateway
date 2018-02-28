package com.szmirren.vxApi.core.handler.freemarker;

import com.szmirren.vxApi.core.handler.freemarker.impl.VxApiFreeMarkerTemplateEngineImpl;

import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;

/**
 * FreeMarker模板Engine
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiFreeMarkerTemplateEngine extends FreeMarkerTemplateEngine {
	static VxApiFreeMarkerTemplateEngine create() {
		return new VxApiFreeMarkerTemplateEngineImpl();
	}

	static VxApiFreeMarkerTemplateEngine create(String templateDirectory) {
		return new VxApiFreeMarkerTemplateEngineImpl(templateDirectory);
	}
}
