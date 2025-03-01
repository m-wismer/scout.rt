/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;

/**
 * The column descriptor class is used to define texts, widths and order of columns. It is typically used for smart
 * fields with a proposal chooser of type table.
 */
public class ColumnDescriptor {

  private String m_propertyName;
  private String m_text;
  private String m_cssClass;
  private int m_width;
  private boolean m_fixedWidth;
  private int m_horizontalAlignment = -1;
  private boolean m_visible = true;
  private boolean m_htmlEnabled = false;

  /**
   * @param propertyName
   *          Name of the corresponding property in the "additional table row data" ({@link AbstractTableRowData}) or
   *          <code>null</code> if this descriptor describes the first (default) column.
   */
  public ColumnDescriptor(String propertyName) {
    m_propertyName = propertyName;
  }

  /**
   * @param propertyName
   *          Name of the corresponding property in the "additional table row data" ({@link AbstractTableRowData}) or
   *          <code>null</code> if this descriptor describes the first (default) column.
   * @param text
   *          Header text of this column.
   * @param width
   *          Width of this column in pixel.
   */
  public ColumnDescriptor(String propertyName, String text, int width) {
    m_propertyName = propertyName;
    m_text = text;
    m_width = width;
  }

  /**
   * @return Name of the corresponding property in the "additional table row data" ({@link AbstractTableRowData}) or
   *         <code>null</code> if this descriptor describes the first (default) column.
   */
  public String getPropertyName() {
    return m_propertyName;
  }

  /**
   * @return Header text of this column.
   */
  public String getText() {
    return m_text;
  }

  /**
   * @param text
   *          Header text of this column.
   */
  public ColumnDescriptor withText(String text) {
    m_text = text;
    return this;
  }

  public String getCssClass() {
    return m_cssClass;
  }

  public ColumnDescriptor withCssClass(String cssClass) {
    m_cssClass = cssClass;
    return this;
  }

  public int getWidth() {
    return m_width;
  }

  public ColumnDescriptor withWidth(int width) {
    m_width = width;
    return this;
  }

  public boolean isFixedWidth() {
    return m_fixedWidth;
  }

  public ColumnDescriptor withFixedWidth(boolean fixedWidth) {
    m_fixedWidth = fixedWidth;
    return this;
  }

  public int getHorizontalAlignment() {
    return m_horizontalAlignment;
  }

  public ColumnDescriptor withHorizontalAlignment(int horizontalAlignment) {
    m_horizontalAlignment = horizontalAlignment;
    return this;
  }

  public boolean isVisible() {
    return m_visible;
  }

  public ColumnDescriptor withVisible(boolean visible) {
    m_visible = visible;
    return this;
  }

  public boolean isHtmlEnabled() {
    return m_htmlEnabled;
  }

  public ColumnDescriptor withHtmlEnabled(boolean htmlEnabled) {
    m_htmlEnabled = htmlEnabled;
    return this;
  }
}
