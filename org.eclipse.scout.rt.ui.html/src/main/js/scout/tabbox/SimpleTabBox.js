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
scout.SimpleTabBox = function() {
  scout.SimpleTabBox.parent.call(this);
  this._addWidgetProperties(['tabArea']);

  this.$body;
  this.htmlComp;
  this.tabArea;
  this.viewStack = [];
  this.currentView;
  this._removeViewInProgress = 0;
};
scout.inherits(scout.SimpleTabBox, scout.Widget);

scout.SimpleTabBox.prototype._init = function(model) {
  scout.SimpleTabBox.parent.prototype._init.call(this, model);
  this.cssClass = model.cssClass;

  if (!this.tabArea) {
    // default tab area
    this.tabArea = scout.create('SimpleTabArea', {
      parent: this
    });
  }
  if (!this.controller) {
    // default controller
    this.controller = scout.create('SimpleTabBoxController');
  }
  // link
  this.controller.install(this, this.tabArea);

  this._viewDestroyedHandler = this._onViewDestroyed.bind(this);
};

/**
 * Returns a $container used as a bind target for the key-stroke context of the group-box.
 * By default this function returns the container of the form, or when group-box is has no
 * form as a parent the container of the group-box.
 */
scout.SimpleTabBox.prototype._keyStrokeBindTarget = function() {
  return this.$container;
};

scout.SimpleTabBox.prototype._render = function() {
  this.$container = this.$parent.appendDiv('simple-tab-box');
  if (this.cssClass) {
    this.$container.addClass(this.cssClass);
  }
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.SimpleTabBoxLayout(this));
  this.htmlComp.layoutData = this.layoutData;

  this.tabArea.render();
  this.$tabArea = this.tabArea.$container;

  // render content
  this.$viewContent = this.$container.appendDiv('tab-content');
  this.viewContent = scout.HtmlComponent.install(this.$viewContent, this.session);
  this.viewContent.setLayout(new scout.SimpleTabViewContentLayout(this));
};

scout.SimpleTabBox.prototype._renderProperties = function() {
  scout.SimpleTabBox.parent.prototype._renderProperties.call(this);
  this._renderView(this.currentView);
};

scout.SimpleTabBox.prototype._renderView = function(view) {
  if (!view) {
    return;
  }
  if (view.rendered) {
    return;
  }
  if (view.tabActivated) {
    view.tabActivated(this.$viewContent);
  } else {
    view.render(this.$viewContent);
  }
  view.$container.addClass('view');
  view.validateRoot = true;
};

scout.SimpleTabBox.prototype.postRender = function() {
  if (this.viewStack.length > 0 && !this.currentView) {
    this.activateView(this.viewStack[this.viewStack.length - 1]);
  }
};

scout.SimpleTabBox.prototype.activateView = function(view) {
  if (view === this.currentView) {
    return;
  }
  console.time('remove')
  if (this.currentView) {
    if (this.currentView.tabDeactivated) {
      this.currentView.tabDeactivated();
    } else {
      this.currentView.remove();
    }
    this.trigger('viewDeactivate', {
      view: this.currentView
    });
    this.currentView = null;
  }
  console.timeEnd('remove')

  // ensure rendered
  if (this.rendered) {
    console.time('render')
    this._renderView(view);
    console.timeEnd('render')
  }

  this.currentView = view;

  console.time('event')
  this.trigger('viewActivate', {
    view: view
  });
  console.timeEnd('event')

  console.time('layout')
  this.revalidateLayout();
  console.timeEnd('layout')
};

scout.SimpleTabBox.prototype.setLayoutData = function(layoutData) {
  scout.SimpleTabBox.parent.prototype.setLayoutData.call(this, layoutData);
  this.layoutData = layoutData;
};

scout.SimpleTabBox.prototype.getLayoutData = function() {
  return this.layoutData;
};

scout.SimpleTabBox.prototype.revalidateLayout = function() {
  if (this.rendered) {
    this.viewContent.invalidateLayoutTree();
    // Layout immediate to prevent 'laggy' form visualization,
    // but not initially while desktop gets rendered because it will be done at the end anyway
    this.viewContent.validateLayoutTree();
  }
};

/**
 *
 * @param view
 * @param bringToTop whether the view should be placed on top of the view stack. the view tab will be selected.
 */
scout.SimpleTabBox.prototype.addView = function(view, bringToTop) {
  var activate = scout.nvl(bringToTop, true);
  // add to view stack
  var siblingView = this._addToViewStack(view, activate);
  // track focus when a view gets removed ond rerendered.
  view.setTrackFocus(true);
  view.setParent(this);
  this.trigger('viewAdd', {
    view: view,
    siblingView: siblingView
  });

  if (activate) {
    this.activateView(view);
  }
};

/**
 * @param view
 * @return the view which is gonna be the sibling to insert the new view tab after.
 */
scout.SimpleTabBox.prototype._addToViewStack = function(view, bringToTop) {
  var sibling;
  var index = this.viewStack.indexOf(view);
  if (index > -1) {
    return this.viewStack[index - 1];
  }

  if (!scout.SimpleTabBoxController.hasViewTab(view)) {
    // first
    this.viewStack.unshift(view);
    this._addDestroyListener(view);
    return sibling;
  }
  if (!this.currentView || !bringToTop) {
    // end
    sibling = this.viewStack[this.viewStack.length - 1];
    this.viewStack.push(view);
    this._addDestroyListener(view);
    return sibling;
  }
  var currentIndex = this.viewStack.indexOf(this.currentView);
  sibling = this.viewStack[currentIndex];
  // it does not matter when index is -1 will be inserted at first position
  this.viewStack.splice(currentIndex + 1, 0, view);
  return sibling;
};

scout.SimpleTabBox.prototype._addDestroyListener = function(view) {
  view.one('destroy', this._viewDestroyedHandler);
};

scout.SimpleTabBox.prototype._removeDestroyListener = function(view) {
  view.off('destroy', this._viewDestroyedHandler);
};

scout.SimpleTabBox.prototype._onViewDestroyed = function(event) {
  var view = event.source;
  scout.arrays.remove(this.viewStack, view);
  if (this.currentView === view) {
    if (this.rendered) {
      view.remove();
    }
    this.currentView = null;
  }
};

scout.SimpleTabBox.prototype.removeView = function(view, showSiblingView) {
  if (!view) {
    return;
  }
  // track focus when a view gets removed ond rerendered.
  view.setTrackFocus(false);
  showSiblingView = scout.nvl(showSiblingView, true);
  var index = this.viewStack.indexOf(view);
  var viewToActivate;
  // if current view is the view to remove reset current view
  if (this.currentView === view) {
    this.currentView = null;
  }

  if (index > -1) {
    // activate previous
    if (showSiblingView) {
      if (index - 1 >= 0) {
        viewToActivate = this.viewStack[index - 1];
      } else if (index + 1 < this.viewStack.length) {
        viewToActivate = this.viewStack[index + 1];
      }
    }

    // remove
    this.viewStack.splice(index, 1);
    if (view.rendered) {
      this._removeViewInProgress++;
      view.remove();
      this._removeViewInProgress--;
    }
    this.trigger('viewRemove', {
      view: view
    });

    if (this._removeViewInProgress === 0) {
      if (viewToActivate) {
        this.activateView(viewToActivate);
      }
      if (this.rendered) {
        this.viewContent.invalidateLayoutTree();
        this.viewContent.validateLayoutTree();
      }
    }
  }
};

scout.SimpleTabBox.prototype.getController = function() {
  return this.controller;
};

scout.SimpleTabBox.prototype.viewCount = function() {
  return this.viewStack.length;
};

scout.SimpleTabBox.prototype.hasViews = function() {
  return this.viewStack.length > 0;
};

scout.SimpleTabBox.prototype.hasView = function(view) {
  return this.viewStack.filter(function(v) {
    return v === view;
  }).length > 0;
};

scout.SimpleTabBox.prototype.getViews = function(displayViewId) {
  return this.viewStack.filter(function(view) {
    if (!displayViewId) {
      return true;
    }
    return displayViewId === view.displayViewId;
  });
};
