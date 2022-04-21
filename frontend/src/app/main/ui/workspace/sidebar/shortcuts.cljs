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
   [app.main.ui.components.dropdown :refer [dropdown]]
   [app.main.ui.icons :as i]
   [app.util.dom :as dom]
   [app.util.i18n :as i18n :refer [tr]]
   [app.util.keyboard :as kbd]
   [cuerdas.core :as str]
   [okulary.core :as l]
   [rumext.alpha :as mf]))

(mf/defc section-dropown
 [{:keys [section sections-elements] :as props}]
 (let [list (filter #(= (key %) section) sections-elements) ]

   [:ul.sub-menu
    (for [command list]
      [:li {:key command}
       [:span "eyyy" (d/name command)]])]))

(mf/defc shortcuts-container
  [{:keys [search-term] :as props}]
  (let [is-macos? (cf/check-platform? :macos)
        dahsboard-shortcuts app.main.data.dashboard.shortcuts/shortcuts
        workspace-shortcuts app.main.data.workspace.shortcuts/shortcuts
        path-shortcuts      app.main.data.workspace.path.shortcuts/shortcuts
        viewer-shortcuts    app.main.data.viewer.shortcuts/shortcuts
        merged-list (merge dahsboard-shortcuts workspace-shortcuts path-shortcuts viewer-shortcuts)
        sections (->> merged-list
                      (mapcat #(:groups (second %))) ;; ¿porque me llega un array en cada paso??
                      (into #{}))
        sections-elements (map (fn [element]
                                 {element (keep (fn [[k v]]
                                                  (when (some #(= element %) (:groups v)) k)) merged-list)}) sections)
        names (keys merged-list)

        grouped-list (group-by :groups merged-list)

        open-section (mf/use-state nil)

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
             (dom/stop-propagation e))))
        
        manage-open
        (mf/use-callback
         (fn [item]
           (fn [event]
             (prn item)
             (.log js/console (clj->js sections-elements) )
             (prn (filter #(= (key %) item) sections-elements))
             (dom/stop-propagation event)
             (reset! open-section item))))]
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
       (for [section sections]
         [:li {:key section
               :on-click (manage-open section)}
          [:span (d/name section)]
          [:& dropdown {:show (= @open-section section)
                        :on-close #(reset! open-section nil)}
           [:ul.sub-menu
            (for [command (get sections-elements section)]
              (prn "eyy" command)
              [:li {:key command}
               [:span "eyyy" (d/name command)]])]]])]]]))