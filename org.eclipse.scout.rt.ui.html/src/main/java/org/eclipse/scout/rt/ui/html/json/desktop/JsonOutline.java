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
package org.eclipse.scout.rt.ui.html.json.desktop;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.IVirtualTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.table.JsonOutlineTable;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;
import org.json.JSONObject;

public class JsonOutline<OUTLINE extends IOutline> extends JsonTree<OUTLINE> {

  private static final String PROP_DETAIL_FORM = IOutline.PROP_DETAIL_FORM;
  private static final String PROP_DETAIL_TABLE = IOutline.PROP_DETAIL_TABLE;
  private static final String PROP_DETAIL_FORM_VISIBLE = "detailFormVisible";
  private static final String PROP_DETAIL_TABLE_VISIBLE = "detailTableVisible";

  private Set<IJsonAdapter<?>> m_jsonDetailTables = new HashSet<IJsonAdapter<?>>();
  private final IDesktop m_desktop;

  public JsonOutline(OUTLINE outline, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(outline, uiSession, id, parent);

    m_desktop = uiSession.getClientSession().getDesktop();
  }

  @Override
  public String getObjectType() {
    return "Outline";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putAdapterIdProperty(json, "defaultDetailForm", getModel().getDefaultDetailForm());
    putAdapterIdsProperty(json, "views", m_desktop.getViews(getModel()));
    putAdapterIdsProperty(json, "dialogs", m_desktop.getDialogs(getModel(), false));
    putAdapterIdsProperty(json, "messageBoxes", m_desktop.getMessageBoxes(getModel()));
    putAdapterIdsProperty(json, "fileChoosers", m_desktop.getFileChoosers(getModel()));
    return json;
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getDefaultDetailForm());

    attachGlobalAdapters(m_desktop.getViews(getModel()));
    attachGlobalAdapters(m_desktop.getDialogs(getModel(), false));
    attachGlobalAdapters(m_desktop.getMessageBoxes(getModel()));
    attachGlobalAdapters(m_desktop.getFileChoosers(getModel()));
  }

  @Override
  protected void attachNode(ITreeNode node, boolean attachChildren) {
    if (attachChildren) {
      attachNodes(node.getChildNodes(), attachChildren);
    }
    if (!(node instanceof IPage)) {
      throw new IllegalArgumentException("Expected page.");
    }
    IPage<?> page = (IPage) node;
    if (page.isDetailFormVisible()) {
      attachGlobalAdapter(page.getDetailForm());
    }
    if (page.isTableVisible()) {
      attachDetailTable(page);
    }
  }

  @Override
  protected JSONObject treeNodeToJson(ITreeNode node) {
    if (!(node instanceof IPage)) {
      throw new IllegalArgumentException("Expected node to be a page. " + node);
    }
    IPage page = (IPage) node;
    JSONObject json = super.treeNodeToJson(node);
    putDetailFormAndTable(json, page);
    putNodeType(json, node);
    if (page.getParentPage() != null) {
      putProperty(json, "lazyAddToTree", page.getParentPage().isLazyAddChildPagesToOutline());
    }
    if (getUiSession().isInspectorHint()) {
      putProperty(json, "modelClass", page.getClass().getName());
      putProperty(json, "classId", page.classId());
    }
    return json;
  }

  private void putNodeType(JSONObject json, ITreeNode node) {
    String nodeType = null;
    if (node instanceof IPageWithNodes) {
      nodeType = "nodes";
    }
    else if (node instanceof IPageWithTable) {
      nodeType = "table";
    }
    else if (node instanceof IVirtualTreeNode) {
      nodeType = "virtual";
    }
    if (nodeType != null) {
      putProperty(json, "nodeType", nodeType);
    }
  }

  protected void putDetailFormAndTable(JSONObject json, IPage page) {
    putProperty(json, PROP_DETAIL_FORM_VISIBLE, page.isDetailFormVisible());
    if (page.isDetailFormVisible()) {
      putAdapterIdProperty(json, PROP_DETAIL_FORM, page.getDetailForm());
    }
    putProperty(json, PROP_DETAIL_TABLE_VISIBLE, page.isTableVisible());
    if (page.isTableVisible()) {
      putAdapterIdProperty(json, PROP_DETAIL_TABLE, page.getTable());
    }
  }

  @Override
  protected void disposeNode(ITreeNode node, boolean disposeChildren) {
    super.disposeNode(node, disposeChildren);

    IPage page = (IPage) node;
    ITable table = page.getTable();
    if (table != null) {
      IJsonAdapter<?> jsonTableAdapter = getGlobalAdapter(table);
      if (jsonTableAdapter != null) {
        // Detail tables are global to make them reusable by other components (e.g. table field)
        // Therefore we have to dispose them by our own
        m_jsonDetailTables.remove(jsonTableAdapter);
        if (!jsonTableAdapter.isDisposed()) {
          jsonTableAdapter.dispose();
        }
      }
    }

    // No need to dispose detail form (it will be disposed automatically when it is closed)
  }

  protected IJsonAdapter<?> attachDetailTable(IPage page) {
    page.getTable().setProperty(JsonOutlineTable.PROP_PAGE, page);
    IJsonAdapter<?> detailTableAdapter = attachGlobalAdapter(page.getTable());
    m_jsonDetailTables.add(detailTableAdapter);
    return detailTableAdapter;
  }

  @Override
  protected void handleModelOtherTreeEvent(TreeEvent event) {
    switch (event.getType()) {
      case OutlineEvent.TYPE_PAGE_CHANGED:
        handleModelPageChanged((OutlineEvent) event);
        break;
      default:
        //NOP
    }
  }

  protected void handleModelPageChanged(OutlineEvent event) {
    IPage page = (IPage) event.getNode();
    attachNode(page, false);

    if (page.isStatusDeleted() || !page.isFilterAccepted()) { // Ignore deleted or filtered nodes, because for the UI, they don't exist
      return;
    }
    String nodeId = getNodeId(page);
    if (nodeId == null) { // Ignore nodes that are not yet sent to the UI (may happen due to asynchronous event processing)
      return;
    }
    JSONObject jsonEvent = JsonObjectUtility.newOrderedJSONObject();
    putProperty(jsonEvent, PROP_NODE_ID, getOrCreateNodeId(page));
    putDetailFormAndTable(jsonEvent, page);
    addActionEvent("pageChanged", jsonEvent);
  }

  @Override
  protected void putUpdatedPropertiesForResolvedNode(JSONObject jsonNode, String nodeId, ITreeNode node, IVirtualTreeNode virtualNode) {
    super.putUpdatedPropertiesForResolvedNode(jsonNode, nodeId, node, virtualNode);
    putNodeType(jsonNode, node);
    if (getUiSession().isInspectorHint()) {
      IPage page = (IPage) node;
      putProperty(jsonNode, "modelClass", page.getClass().getName());
      putProperty(jsonNode, "classId", page.classId());
    }
  }
}
