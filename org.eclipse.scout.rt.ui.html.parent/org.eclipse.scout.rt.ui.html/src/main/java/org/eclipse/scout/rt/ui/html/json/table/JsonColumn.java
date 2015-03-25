/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ICustomColumn;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.json.JSONObject;

public class JsonColumn<T extends IColumn<?>> implements IJsonObject {

  private IJsonSession m_jsonSession;
  private T m_column;
  private int m_indexOffset;

  public JsonColumn(T model, IJsonSession jsonSession) {
    m_column = model;
    m_jsonSession = jsonSession;
  }

  public String getObjectType() {
    return "TableColumn";
  }

  public void setColumnIndexOffset(int indexOffset) {
    m_indexOffset = indexOffset;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    JsonObjectUtility.putProperty(json, "id", getColumn().getColumnId());
    JsonObjectUtility.putProperty(json, "objectType", getObjectType());
    JsonObjectUtility.putProperty(json, "index", getColumn().getColumnIndex() - m_indexOffset);
    JsonObjectUtility.putProperty(json, "text", getJsonSession().getCustomHtmlRenderer().convert(getColumn().getHeaderCell().getText(), true));
    JsonObjectUtility.putProperty(json, "type", computeColumnType(getColumn()));
    JsonObjectUtility.putProperty(json, IColumn.PROP_WIDTH, getColumn().getWidth());
    if (getColumn().getInitialWidth() != getColumn().getWidth()) {
      JsonObjectUtility.putProperty(json, "initialWidth", getColumn().getInitialWidth());
    }
    JsonObjectUtility.putProperty(json, "summary", getColumn().isSummary());
    JsonObjectUtility.putProperty(json, IColumn.PROP_HORIZONTAL_ALIGNMENT, getColumn().getHorizontalAlignment());
    if (getColumn().isSortActive() && getColumn().isSortExplicit()) {
      JsonObjectUtility.putProperty(json, "sortActive", true);
      JsonObjectUtility.putProperty(json, "sortAscending", getColumn().isSortAscending());
      JsonObjectUtility.putProperty(json, "sortIndex", getColumn().getSortIndex());
    }
    if (getColumn() instanceof ICustomColumn) {
      JsonObjectUtility.putProperty(json, "custom", true);
    }
    JsonObjectUtility.putProperty(json, IColumn.PROP_FIXED_WIDTH, getColumn().isFixedWidth());
    JsonObjectUtility.putProperty(json, IColumn.PROP_EDITABLE, getColumn().isEditable());
    // FIXME CGU: complete
    return json;
  }

  protected String computeColumnType(IColumn column) {
    if (column instanceof INumberColumn) {
      return "number";
    }
    if (column instanceof IDateColumn) {
      return "date";
    }
    return "text";
  }

  public Object cellValueToJson(Object value) {
    // In most cases it is not necessary to send the value to the client because text is sufficient
    return null;
  }

  public T getColumn() {
    return m_column;
  }

  public IJsonSession getJsonSession() {
    return m_jsonSession;
  }

}
