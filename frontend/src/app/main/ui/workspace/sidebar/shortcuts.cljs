;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) UXBOX Labs SL

(ns app.main.ui.workspace.sidebar.shortcuts
  (:require
   [app.common.data :as d]
   [app.config :as cf]
   [app.main.data.dashboard.shortcuts]
   [app.main.data.viewer.shortcuts]
   [app.main.data.workspace :as dw]
   [app.main.data.workspace.path.shortcuts]
   [app.main.data.workspace.shortcuts]
   [app.main.refs :as refs]
   [app.main.store :as st]
   [app.main.ui.icons :as i]
   [app.util.dom :as dom]
   [app.util.i18n :as i18n :refer [tr]]
   [app.util.keyboard :as kbd]
   [cuerdas.core :as str]
   [okulary.core :as l]
   [rumext.alpha :as mf]))

(mf/defc shortcuts-container
  [{:keys [search-term team-id] :as props}]
  (let [is-macos? (cf/check-platform? :macos)
        dahsboard-shortcuts app.main.data.dashboard.shortcuts/shortcuts
        workspace-shortcuts app.main.data.workspace.shortcuts/shortcuts
        path-shortcuts      app.main.data.workspace.path.shortcuts/shortcuts
        viewer-shortcuts    app.main.data.viewer.shortcuts/shortcuts
        shortcuts-full-list {:dashboard-shortcuts dahsboard-shortcuts
                             :workspace-shortcuts workspace-shortcuts
                             :path-shortcuts path-shortcuts
                             :viewer-shortcuts viewer-shortcuts}
        merged-list (merge dahsboard-shortcuts workspace-shortcuts path-shortcuts viewer-shortcuts)
        names (keys merged-list)
        grouped-list (group-by :groups merged-list)
        _ (.log js/console (clj->js names))

        close-fn #(st/emit! (dw/toggle-layout-flag :shortcuts))
        search-term (or search-term "")
        on-search-focus
        (mf/use-callback

         (fn [event]))

        on-search-blur
        (mf/use-callback
         (fn [_]))

        on-search-change
        (mf/use-callback
         (fn [event]
           (let [value (dom/get-target-val event)])))

        on-clear-click
        (mf/use-callback

         (fn [_]
           (let [search-input (dom/get-element "search-input")]
             (dom/clean-value! search-input)
             (dom/focus! search-input))))

        on-key-press
        (mf/use-callback
         (fn [e]
           (when (kbd/enter? e)
             (dom/prevent-default e)
             (dom/stop-propagation e))))]
    [:div.shortcuts
     [:div.shortcuts-header
      [:div.shortcuts-close-button
       {:on-click close-fn} i/close]
      [:div.shortcuts-title "Keyboard shortcuts"]]
     [:div.search-field
      [:span.search-box
       [:input.input-text
        {:key "shortcuts-search-box"
         :id "shortcut-search"
         :type "text"
         :placeholder "Search shortcuts"
         :default-value search-term
         :auto-complete "off"
         :on-focus on-search-focus
         :on-blur on-search-blur
         :on-change on-search-change
         :on-key-press on-key-press
         :ref #(when % (set! (.-value %) search-term))}]
       [:span.icon-wrapper i/search]]]
     [:div.shortcut-list
      [:ul
       (for [shortcut merged-list]
         (prn shortcut)
         [:li {:key (:command shortcut)}
          [:span (:command shortcut)]])]]]))