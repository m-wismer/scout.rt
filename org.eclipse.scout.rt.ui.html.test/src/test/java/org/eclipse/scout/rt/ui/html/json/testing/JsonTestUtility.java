/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingListener;

import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.resource.MimeType;
import org.eclipse.scout.rt.server.commons.BufferedServletOutputStream;
import org.eclipse.scout.rt.server.commons.HttpSessionMutex;
import org.eclipse.scout.rt.ui.html.HttpSessionHelper;
import org.eclipse.scout.rt.ui.html.ISessionStore;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiSession;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonRequest;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.JsonStartupRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public final class JsonTestUtility {

  private JsonTestUtility() {
  }

  @SuppressWarnings("resource")
  public static IUiSession createAndInitializeUiSession() {
    final HttpSession httpSession = createHttpSession(new Object());
    final HttpServletRequest request = createHttpServletRequest(httpSession, "/json", null);
    final HttpServletResponse response = createHttpServletResponse(new BufferedServletOutputStream());

    String clientSessionId = "testClientSession123";
    JSONObject jsonReqObj = new JSONObject();
    jsonReqObj.put(JsonStartupRequest.PROP_CLIENT_SESSION_ID, clientSessionId);
    jsonReqObj.put("startup", true);
    JsonStartupRequest jsonStartupRequest = new JsonStartupRequest(new JsonRequest(jsonReqObj));
    IUiSession uiSession = new TestEnvironmentUiSession();
    uiSession.init(request, response, jsonStartupRequest);
    return uiSession;
  }

  public static HttpSession createHttpSession(Object sessionMutex) {
    final HttpSession httpSession = Mockito.mock(HttpSession.class);
    Mockito.when(httpSession.getAttribute(HttpSessionMutex.SESSION_MUTEX_ATTRIBUTE_NAME)).thenReturn(sessionMutex);
    final ISessionStore sessionStore = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    Mockito.when(httpSession.getAttribute(HttpSessionHelper.SESSION_STORE_ATTRIBUTE_NAME)).thenReturn(sessionStore);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) {
        ((HttpSessionBindingListener) sessionStore).valueUnbound(null);
        return null;
      }
    }).when(httpSession).invalidate();
    return httpSession;
  }

  public static HttpServletRequest createHttpServletRequest(HttpSession httpSession, String pathInfo, String jsonData) {
    HttpServletRequest mock = Mockito.mock(HttpServletRequest.class);
    Mockito.when(mock.getLocale()).thenReturn(new Locale("de_CH"));
    Mockito.when(mock.getHeader("User-Agent")).thenReturn("dummy");
    Mockito.when(mock.getPathInfo()).thenReturn(pathInfo);
    Mockito.when(mock.getContentType()).thenReturn(MimeType.JSON.getType());
    Mockito.when(mock.getSession()).thenReturn(httpSession);
    Mockito.when(mock.getSession(false)).thenReturn(httpSession);
    if (jsonData != null) {
      try {
        Mockito.when(mock.getReader()).thenReturn(new BufferedReader(new StringReader(jsonData)));
      }
      catch (IOException e) {
        throw new PlatformException("Cannot create StringReader", e);
      }
    }
    return mock;
  }

  public static HttpServletResponseWrapper createHttpServletResponse(final ServletOutputStream out) {
    HttpServletResponse mock = Mockito.mock(HttpServletResponse.class);
    HttpServletResponseWrapper resp = new HttpServletResponseWrapper(mock) {
      private String m_contentType;
      private String m_characterEncoding;

      @Override
      public String getContentType() {
        return m_contentType;
      }

      @Override
      public void setContentType(String type) {
        m_contentType = type;
      }

      @Override
      public void setContentLength(int len) {
      }

      @Override
      public void setContentLengthLong(long len) {
      }

      @Override
      public void setCharacterEncoding(String charset) {
        m_characterEncoding = charset;
      }

      @Override
      public String getCharacterEncoding() {
        return m_characterEncoding;
      }

      @Override
      public ServletOutputStream getOutputStream() throws IOException {
        return out;
      }
    };
    return resp;
  }

  /**
   * Empties the response object and flushes the session
   */
  public static void endRequest(UiSession uiSession) throws Exception {
    UiSessionTestUtility.endRequest(uiSession);
  }

  /**
   * Ensures that all buffered events are applied to the JSON-Adapters.
   */
  public static void processBufferedEvents(IUiSession uiSession) {
    uiSession.currentJsonResponse().fireProcessBufferedEvents();
  }

  /**
   * @param eventType
   *          Optional. If set only events with the given type will be returned.
   * @param adapterId
   *          Optional. If set only events for the given id will be returned.
   */
  public static List<JsonEvent> extractEventsFromResponse(JsonResponse response, String eventType, String adapterId) throws JSONException {
    response.fireProcessBufferedEvents();
    List<JsonEvent> list = new ArrayList<>();
    for (JsonEvent event : response.getEventList()) {
      if ((eventType == null || event.getType().equals(eventType)) &&
          (adapterId == null || event.getTarget().equals(adapterId))) {
        list.add(event);
      }
    }
    return list;
  }

  public static List<JsonEvent> extractEventsFromResponse(JsonResponse response, String eventType) throws JSONException {
    return extractEventsFromResponse(response, eventType, null);
  }

  public static List<JsonEvent> extractPropertyChangeEvents(JsonResponse response, String adapterId) throws JSONException {
    return extractEventsFromResponse(response, JsonEventType.PROPERTY.getEventType(), adapterId);
  }

  public static <T> T extractProperty(JsonResponse response, String adapterId, String propertyName) throws JSONException {
    List<JsonEvent> properties = JsonTestUtility.extractPropertyChangeEvents(response, adapterId);
    if (properties.size() > 0) {
      return extractProperty(properties.get(0).getData(), propertyName);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <T> T extractProperty(JSONObject data, String propertyName) throws JSONException {
    return (T) data.getJSONObject("properties").get(propertyName);
  }

  public static JSONObject getAdapterData(JSONObject json, String id) throws JSONException {
    return json.getJSONObject(JsonResponse.PROP_ADAPTER_DATA).getJSONObject(id);
  }

  public static JSONObject getEvent(JSONObject json, int index) throws JSONException {
    return (JSONObject) json.getJSONArray(JsonResponse.PROP_EVENTS).get(index);
  }

  public static JSONObject getPropertyChange(JSONObject json, int index) throws JSONException {
    return getEvent(json, index).getJSONObject("properties");
  }

  public static void initField(ICompositeField compositeField) {
    InitFieldVisitor visitor = new InitFieldVisitor();
    compositeField.visitFields(visitor, 0);
    visitor.handleResult();
  }

  // copy from FormUtility
  private static class InitFieldVisitor implements IFormFieldVisitor {
    private RuntimeException m_firstEx;

    @Override
    public boolean visitField(IFormField field, int level, int fieldIndex) {
      try {
        field.initField();
      }
      catch (RuntimeException e) {
        if (m_firstEx == null) {
          m_firstEx = e;
        }
      }
      return true;
    }

    public void handleResult() {
      if (m_firstEx != null) {
        throw m_firstEx;
      }
    }
  }

}
