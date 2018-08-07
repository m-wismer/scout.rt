/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.CompactTree = function() {
  scout.CompactTree.parent.call(this);
  this.$nodesContainer;
  this._scrolldirections = 'y';
};
scout.inherits(scout.CompactTree, scout.Tree);

/**
 * @override Tree.js
 */
scout.CompactTree.prototype._initTreeKeyStrokeContext = function() {
  this.keyStrokeContext.registerKeyStroke([
    new scout.CompactTreeUpKeyStroke(this),
    new scout.CompactTreeDownKeyStroke(this),
    new scout.CompactTreeLeftKeyStroke(this),
    new scout.CompactTreeRightKeyStroke(this)
  ]);
};

scout.CompactTree.prototype._createTreeNode = function(nodeModel) {
  nodeModel = scout.nvl(nodeModel, {});
  nodeModel.parent = this;
  return scout.create('CompactTreeNode', nodeModel);
};

scout.CompactTree.prototype._render = function() {
  this.$container = this.$parent.appendDiv('compact-tree');

  var layout = new scout.TreeLayout(this);
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(layout);

  this.$data = this.$container.appendDiv('tree-data');
  this._installScrollbars({
    borderless: true
  });
  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.MenuItemsOrder(this.session, 'Tree')
  });
  this.menuBar.render();

  this.$nodesContainer = this.$data.appendDiv('nodes');
  this._updateNodeDimensions();
  this._renderViewport();
  this.invalidateLayoutTree();
};

/**
 * @override
 */
scout.CompactTree.prototype._calculateCurrentViewRange = function() {
  this.viewRangeSize = this.visibleNodesFlat.length;
  return new scout.Range(0, Math.max(this.visibleNodesFlat.length, 0));
};

/**
 * @override
 */
scout.CompactTree.prototype.calculateViewRangeSize = function() {
  return this.visibleNodesFlat.length;
};

/**
 * @override
 */
scout.CompactTree.prototype._insertNodeInDOMAtPlace = function(node, index) {
  var visibleNodeBefore = this.visibleNodesFlat[index - 1];
  var n;
  if (!visibleNodeBefore) {
    node.$node.prependTo(this.$nodesContainer);
  } else if (visibleNodeBefore.level < node.level) {
    //insert after first child node (title from the level above)
    node.$node.insertAfter(visibleNodeBefore.$node.children()[0]);
  } else {
    n = visibleNodeBefore.$node;
    for (var i = 0; i < visibleNodeBefore.level - node.level; i++) {
      n = n.parent();
    }
    node.$node.insertAfter(n);
  }
};

/**
 * @override
 */
scout.CompactTree.prototype.selectNodes = function(nodes, debounceSend) {
  var selectedSectionNodes = [];
  nodes = scout.arrays.ensure(nodes);
  nodes.forEach(function(node) {
    // If a section is selected, automatically change selection to first section-node
    if (node.isSection()) {
      if (node.childNodes.length > 0) {
        selectedSectionNodes.push(node.childNodes[0]);
      }
    } else {
      selectedSectionNodes.push(node);
    }
  }, this);

  scout.CompactTree.parent.prototype.selectNodes.call(this, selectedSectionNodes);
};

/**
 * @override
 */
scout.CompactTree.prototype._renderExpansion = function(node) {
  // nop (not supported by CompactTree)
};

/**
 * @override
 */
scout.CompactTree.prototype._updateItemPath = function() {
  // nop (not supported by CompactTree)
};

