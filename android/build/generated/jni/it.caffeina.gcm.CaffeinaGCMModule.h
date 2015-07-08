/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

/** This is generated, do not edit by hand. **/

#include <jni.h>

#include "Proxy.h"

		namespace it {
		namespace caffeina {
		namespace gcm {


class CaffeinaGCMModule : public titanium::Proxy
{
public:
	explicit CaffeinaGCMModule(jobject javaObject);

	static void bindProxy(v8::Handle<v8::Object> exports);
	static v8::Handle<v8::FunctionTemplate> getProxyTemplate();
	static void dispose();

	static v8::Persistent<v8::FunctionTemplate> proxyTemplate;
	static jclass javaClass;

private:
	// Methods -----------------------------------------------------------
	static v8::Handle<v8::Value> getLastData(const v8::Arguments&);
	static v8::Handle<v8::Value> unregisterForPushNotifications(const v8::Arguments&);
	static v8::Handle<v8::Value> registerForPushNotifications(const v8::Arguments&);
	static v8::Handle<v8::Value> clearLastData(const v8::Arguments&);
	static v8::Handle<v8::Value> getRegistrationId(const v8::Arguments&);

	// Dynamic property accessors ----------------------------------------
	static v8::Handle<v8::Value> getter_registrationId(v8::Local<v8::String> property, const v8::AccessorInfo& info);
	static v8::Handle<v8::Value> getter_lastData(v8::Local<v8::String> property, const v8::AccessorInfo& info);

};

		} // gcm
		} // caffeina
		} // it
