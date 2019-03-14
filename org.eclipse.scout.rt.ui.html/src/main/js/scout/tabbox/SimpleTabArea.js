/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.SimpleTabArea = function() {
  scout.SimpleTabArea.parent.call(this);
  this.tabs = [];
};
scout.inherits(scout.SimpleTabArea, scout.Widget);

scout.SimpleTabArea.prototype._init = function(model) {
  scout.SimpleTabArea.parent.prototype._init.call(this, model);
  this._selectedViewTab;

  this._tabClickHandler = this._onTabClick.bind(this);
  this.htmlComp = new scout.HtmlComponent(null, this.session);
};

scout.SimpleTabArea.prototype._render = function() {
  this.$container = this.$parent.appendDiv('simple-tab-area');
//  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.bind(this.$container);
  this.htmlComp.setLayout(new scout.SimpleTabAreaLayout(this));
};

scout.SimpleTabArea.prototype._renderProperties = function() {
  scout.SimpleTabArea.parent.prototype._renderProperties.call(this);
  this._renderTabs();
};

scout.SimpleTabArea.prototype._renderTabs = function() {
  // reverse since tab.renderAfter() called without sibling=true argument (see _renderTab)
  // will _prepend_ themselves into the container.
  this.tabs.slice().reverse()
    .forEach(function(tab) {
      this._renderTab(tab);
    }.bind(this));
};

scout.SimpleTabArea.prototype._renderTab = function(tab) {
  tab.renderAfter(this.$container);
};

scout.SimpleTabArea.prototype.setVisible = function(visible) {
  scout.SimpleTabArea.parent.prototype.setVisible.call(this, visible);
};

scout.SimpleTabArea.prototype._renderVisible = function() {
  scout.SimpleTabArea.parent.prototype._renderVisible.call(this);
  this.invalidateLayoutTree();
};

scout.SimpleTabArea.prototype._onTabClick = function(event) {
  this.selectTab(event.source);
};

scout.SimpleTabArea.prototype.getTabs = function() {
  return this.tabs;
};

scout.SimpleTabArea.prototype.selectTab = function(viewTab) {
  if (this._selectedViewTab === viewTab) {
    return;
  }
  this.deselectTab(this._selectedViewTab);
  this._selectedViewTab = viewTab;
  if (viewTab) {
    // Select the new view tab.
    viewTab.select();
  }
  this.trigger('tabSelect', {
    viewTab: viewTab
  });
  if (viewTab && viewTab.rendered && !viewTab.$container.isVisible()) {
    this.invalidateLayoutTree();
  }
};

scout.SimpleTabArea.prototype.deselectTab = function(viewTab) {
  if (!viewTab) {
    return;
  }
  if (this._selectedViewTab !== viewTab) {
    return;
  }
  this._selectedViewTab.deselect();
};

scout.SimpleTabArea.prototype.getSelectedTab = function() {
  return this._selectedViewTab;
};

scout.SimpleTabArea.prototype.addTab = function(tab, sibling) {
  var insertPosition = -1;
  if (sibling) {
    insertPosition = this.tabs.indexOf(sibling);
  }
  this.tabs.splice(insertPosition + 1, 0, tab);
  tab.on('click', this._tabClickHandler);
  if (this.rendered) {
    this._renderVisible();
    tab.renderAfter(this.$container, sibling);
    this.invalidateLayoutTree();
  }
};

scout.SimpleTabArea.prototype.destroyTab = function(tab) {
  var index = this.tabs.indexOf(tab);
  if (index > -1) {
    this.tabs.splice(index, 1);
    tab.destroy();
    tab.off('click', this._tabClickHandler);
    this._renderVisible();
    this.invalidateLayoutTree();
  }
};
