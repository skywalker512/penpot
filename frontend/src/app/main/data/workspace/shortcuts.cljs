;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) UXBOX Labs SL

(ns app.main.data.workspace.shortcuts
  (:require
   [app.main.data.events :as ev]
   [app.main.data.exports :as de]
   [app.main.data.shortcuts :as ds]
   [app.main.data.workspace :as dw]
   [app.main.data.workspace.colors :as mdc]
   [app.main.data.workspace.common :as dwc]
   [app.main.data.workspace.drawing :as dwd]
   [app.main.data.workspace.layers :as dwly]
   [app.main.data.workspace.libraries :as dwl]
   [app.main.data.workspace.texts :as dwtxt]
   [app.main.data.workspace.transforms :as dwt]
   [app.main.data.workspace.undo :as dwu]
   [app.main.store :as st]
   [app.main.ui.hooks.resize :as r]
   [app.util.dom :as dom]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Shortcuts
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn toggle-layout-flag
  [flag]
  (-> (dw/toggle-layout-flag flag)
      (vary-meta assoc ::ev/origin "workspace-shortcuts")))

;; Shortcuts format https://github.com/ccampbell/mousetrap

(def base-shortcuts
  {;; EDIT
   :undo                 {:tooltip (ds/meta "Z")
                          :command (ds/c-mod "z")
                          :groups [:edit]
                          :fn #(st/emit! dwc/undo)}

   :redo                 {:tooltip (ds/meta "Y")
                          :command [(ds/c-mod "shift+z") (ds/c-mod "y")]
                          :groups [:edit]
                          :fn #(st/emit! dwc/redo)}

   :clear-undo           {:tooltip (ds/meta "Q")
                          :command (ds/c-mod "q")
                          :groups [:edit]
                          :fn #(st/emit! dwu/reinitialize-undo)}

   :copy                 {:tooltip (ds/meta "C")
                          :command (ds/c-mod "c")
                          :groups [:edit]
                          :fn #(st/emit! (dw/copy-selected))}

   :cut                  {:tooltip (ds/meta "X")
                          :command (ds/c-mod "x")
                          :groups [:edit]
                          :fn #(st/emit! (dw/copy-selected)
                                         (dw/delete-selected))}

   :paste                {:tooltip (ds/meta "V")
                          :disabled true
                          :command (ds/c-mod "v")
                          :groups [:edit]
                          :fn (constantly nil)}

   :delete               {:tooltip (ds/supr)
                          :command ["del" "backspace"]
                          :groups [:edit]
                          :fn #(st/emit! (dw/delete-selected))}

   :duplicate            {:tooltip (ds/meta "D")
                          :command (ds/c-mod "d")
                          :groups [:edit]
                          :fn #(st/emit! (dw/duplicate-selected true))}

   :start-editing        {:tooltip (ds/enter)
                          :command "enter"
                          :groups [:edit]
                          :fn #(st/emit! (dw/start-editing-selected))}

   :start-measure        {:tooltip (ds/alt "")
                          :command ["alt" "."]
                          :type "keydown"
                          :groups [:edit]
                          :fn #(st/emit! (dw/toggle-distances-display true))}

   :stop-measure         {:tooltip (ds/alt "")
                          :command ["alt" "."]
                          :type "keyup"
                          :groups [:edit]
                          :fn #(st/emit! (dw/toggle-distances-display false))}

   :escape               {:tooltip (ds/esc)
                          :command "escape"
                          :groups [:edit]
                          :fn #(st/emit! :interrupt (dw/deselect-all true))}

   ;; MODIFY LAYERS


   :group                {:tooltip (ds/meta "G")
                          :command (ds/c-mod "g")
                          :groups [:modify-layers]
                          :fn #(st/emit! dw/group-selected)}

   :ungroup              {:tooltip (ds/shift "G")
                          :command "shift+g"
                          :groups [:modify-layers]
                          :fn #(st/emit! dw/ungroup-selected)}

   :mask                 {:tooltip (ds/meta "M")
                          :command (ds/c-mod "m")
                          :groups [:modify-layers]
                          :fn #(st/emit! dw/mask-group)}

   :unmask               {:tooltip (ds/meta-shift "M")
                          :command (ds/c-mod "shift+m")
                          :groups [:modify-layers]
                          :fn #(st/emit! dw/unmask-group)}

   :create-component     {:tooltip (ds/meta "K")
                          :command (ds/c-mod "k")
                          :groups [:modify-layers]
                          :fn #(st/emit! (dwl/add-component))}

   :detach-component     {:tooltip (ds/meta-shift "K")
                          :command (ds/c-mod "shift+k")
                          :groups [:modify-layers]
                          :fn #(st/emit! dwl/detach-selected-components)}

   :flip-vertical        {:tooltip (ds/shift "V")
                          :command "shift+v"
                          :groups [:modify-layers]
                          :fn #(st/emit! (dw/flip-vertical-selected))}

   :flip-horizontal      {:tooltip (ds/shift "H")
                          :command "shift+h"
                          :groups [:modify-layers]
                          :fn #(st/emit! (dw/flip-horizontal-selected))}
   :bring-forward        {:tooltip (ds/meta ds/up-arrow)
                          :command (ds/c-mod "up")
                          :groups [:modify-layers]
                          :fn #(st/emit! (dw/vertical-order-selected :up))}

   :bring-backward       {:tooltip (ds/meta ds/down-arrow)
                          :command (ds/c-mod "down")
                          :groups [:modify-layers]
                          :fn #(st/emit! (dw/vertical-order-selected :down))}

   :bring-front          {:tooltip (ds/meta-shift ds/up-arrow)
                          :command (ds/c-mod "shift+up")
                          :groups [:modify-layers]
                          :fn #(st/emit! (dw/vertical-order-selected :top))}

   :bring-back           {:tooltip (ds/meta-shift ds/down-arrow)
                          :command (ds/c-mod "shift+down")
                          :groups [:modify-layers]
                          :fn #(st/emit! (dw/vertical-order-selected :bottom))}

   :move-fast-up         {:tooltip (ds/shift ds/up-arrow)
                          :command "shift+up"
                          :groups [:modify-layers]
                          :fn #(st/emit! (dwt/move-selected :up true))}

   :move-fast-down       {:tooltip (ds/shift ds/down-arrow)
                          :command "shift+down"
                          :groups [:modify-layers]
                          :fn #(st/emit! (dwt/move-selected :down true))}

   :move-fast-right      {:tooltip (ds/shift ds/right-arrow)
                          :command "shift+right"
                          :groups [:modify-layers]
                          :fn #(st/emit! (dwt/move-selected :right true))}

   :move-fast-left       {:tooltip (ds/shift ds/left-arrow)
                          :command "shift+left"
                          :groups [:modify-layers]
                          :fn #(st/emit! (dwt/move-selected :left true))}

   :move-unit-up         {:tooltip ds/up-arrow
                          :command "up"
                          :groups [:modify-layers]
                          :fn #(st/emit! (dwt/move-selected :up false))}

   :move-unit-down       {:tooltip ds/down-arrow
                          :command "down"
                          :groups [:modify-layers]
                          :fn #(st/emit! (dwt/move-selected :down false))}

   :move-unit-left       {:tooltip ds/right-arrow
                          :command "right"
                          :groups [:modify-layers]
                          :fn #(st/emit! (dwt/move-selected :right false))}

   :move-unit-right      {:tooltip ds/left-arrow
                          :command "left"
                          :groups [:modify-layers]
                          :fn #(st/emit! (dwt/move-selected :left false))}

   ;; TOOLS

   :draw-frame           {:tooltip "A"
                          :command "a"
                          :groups [:tools :basics]
                          :fn #(st/emit! (dwd/select-for-drawing :frame))}

   :move                 {:tooltip "V"
                          :command "v"
                          :groups [:tools]
                          :fn #(st/emit! :interrupt)}

   :draw-rect            {:tooltip "R"
                          :command "r"
                          :groups [:tools]
                          :fn #(st/emit! (dwd/select-for-drawing :rect))}

   :draw-ellipse         {:tooltip "E"
                          :command "e"
                          :groups [:tools]
                          :fn #(st/emit! (dwd/select-for-drawing :circle))}

   :draw-text            {:tooltip "T"
                          :command "t"
                          :groups [:tools]
                          :fn #(st/emit! dwtxt/start-edit-if-selected
                                         (dwd/select-for-drawing :text))}

   :draw-path            {:tooltip "P"
                          :command "p"
                          :groups [:tools]
                          :fn #(st/emit! (dwd/select-for-drawing :path))}

   :draw-curve           {:tooltip (ds/shift "C")
                          :command "shift+c"
                          :groups [:tools]
                          :fn #(st/emit! (dwd/select-for-drawing :curve))}

   :add-comment          {:tooltip "C"
                          :command "c"
                          :groups [:tools]
                          :fn #(st/emit! (dwd/select-for-drawing :comments))}

   :insert-image         {:tooltip (ds/shift "K")
                          :command "shift+k"
                          :groups [:tools]
                          :fn #(-> "image-upload" dom/get-element dom/click)}

   :toggle-visibility    {:tooltip (ds/meta-shift "H")
                          :command (ds/c-mod "shift+h")
                          :groups [:tools]
                          :fn #(st/emit! (dw/toggle-visibility-selected))}

   :toggle-lock          {:tooltip (ds/meta-shift "L")
                          :command (ds/c-mod "shift+l")
                          :groups [:tools]
                          :fn #(st/emit! (dw/toggle-lock-selected))}

   :toggle-lock-size     {:tooltip (ds/meta (ds/alt "L"))
                          :command (ds/c-mod "alt+l")
                          :groups [:tools]
                          :fn #(st/emit! (dw/toggle-proportion-lock))}


   ;; ITEM ALIGNEMENT

   :align-left           {:tooltip (ds/alt "A")
                          :command "alt+a"
                          :groups [:alignement]
                          :fn #(st/emit! (dw/align-objects :hleft))}

   :align-right          {:tooltip (ds/alt "D")
                          :command "alt+d"
                          :groups [:alignement]
                          :fn #(st/emit! (dw/align-objects :hright))}

   :align-top            {:tooltip (ds/alt "W")
                          :command "alt+w"
                          :groups [:alignement]
                          :fn #(st/emit! (dw/align-objects :vtop))}

   :align-hcenter        {:tooltip (ds/alt "H")
                          :command "alt+h"
                          :groups [:alignement]
                          :fn #(st/emit! (dw/align-objects :hcenter))}

   :align-vcenter        {:tooltip (ds/alt "V")
                          :command "alt+v"
                          :groups [:alignement]
                          :fn #(st/emit! (dw/align-objects :vcenter))}

   :align-bottom         {:tooltip (ds/alt "S")
                          :command "alt+s"
                          :groups [:alignement]
                          :fn #(st/emit! (dw/align-objects :vbottom))}

   :h-distribute         {:tooltip (ds/meta-shift (ds/alt "H"))
                          :command (ds/c-mod "shift+alt+h")
                          :groups [:alignement]
                          :fn #(st/emit! (dw/distribute-objects :horizontal))}

   :v-distribute         {:tooltip (ds/meta-shift (ds/alt "V"))
                          :command (ds/c-mod "shift+alt+v")
                          :groups [:alignement]
                          :fn #(st/emit! (dw/distribute-objects :vertical))}

   ;; MAIN MENU

   :toggle-rules         {:tooltip (ds/meta-shift "R")
                          :command (ds/c-mod "shift+r")
                          :groups [:main-menu]
                          :fn #(st/emit! (toggle-layout-flag :rules))}

   :select-all           {:tooltip (ds/meta "A")
                          :command (ds/c-mod "a")
                          :groups [:main-menu]
                          :fn #(st/emit! (dw/select-all))}

   :toggle-grid          {:tooltip (ds/meta "'")
                          :command (ds/c-mod "'")
                          :groups [:main-menu]
                          :fn #(st/emit! (toggle-layout-flag :display-grid))}

   :toggle-snap-grid     {:tooltip (ds/meta-shift "'")
                          :command (ds/c-mod "shift+'")
                          :groups [:main-menu]
                          :fn #(st/emit! (toggle-layout-flag :snap-grid))}

   :toggle-alignment     {:tooltip (ds/meta "\\")
                          :command (ds/c-mod "\\")
                          :groups [:main-menu]
                          :fn #(st/emit! (toggle-layout-flag :dynamic-alignment))}

   ;; PANELS

   :toggle-layers       {:tooltip (ds/alt "L")
                         :command (ds/a-mod "l")
                         :groups [:panels]
                         :fn #(st/emit! (dw/go-to-layout :layers))}

   :toggle-assets       {:tooltip (ds/alt "I")
                         :command (ds/a-mod "i")
                         :groups [:panels]
                         :fn #(st/emit! (dw/go-to-layout :assets))}

   :toggle-history      {:tooltip (ds/alt "H")
                         :command (ds/a-mod "h")
                         :groups [:panels]
                         :fn #(st/emit! (dw/go-to-layout :document-history))}

   :toggle-colorpalette {:tooltip (ds/alt "P")
                         :command (ds/a-mod "p")
                         :groups [:panels]
                         :fn #(do (r/set-resize-type! :bottom)
                                  (st/emit! (dw/remove-layout-flag :textpalette)
                                            (toggle-layout-flag :colorpalette)))}

   :toggle-textpalette  {:tooltip (ds/alt "T")
                         :command (ds/a-mod "t")
                         :groups [:panels]
                         :fn #(do (r/set-resize-type! :bottom)
                                  (st/emit! (dw/remove-layout-flag :colorpalette)
                                            (toggle-layout-flag :textpalette)))}

   :hide-ui              {:tooltip "\\"
                          :command "\\"
                          :groups [:panels :basic]
                          :fn #(st/emit! (toggle-layout-flag :hide-ui))}

   ;; ZOOM-WORKSPACE

   :increase-zoom        {:tooltip "+"
                          :command ["+" "="]
                          :groups [:zoom :zoom-workspace]
                          :fn #(st/emit! (dw/increase-zoom nil))}

   :decrease-zoom        {:tooltip "-"
                          :command ["-" "_"]
                          :groups [:zoom :zoom-workspace]
                          :fn #(st/emit! (dw/decrease-zoom nil))}

   :reset-zoom           {:tooltip (ds/shift "0")
                          :command "shift+0"
                          :groups [:zoom :zoom-workspace]
                          :fn #(st/emit! dw/reset-zoom)}

   :fit-all              {:tooltip (ds/shift "1")
                          :command "shift+1"
                          :groups [:zoom :zoom-workspace]
                          :fn #(st/emit! dw/zoom-to-fit-all)}

   :zoom-selected        {:tooltip (ds/shift "2")
                          :command ["shift+2" "@" "\""]
                          :groups [:zoom :zoom-workspace]
                          :fn #(st/emit! dw/zoom-to-selected-shape)}

   ;; NAVIGATION


   :open-viewer          {:tooltip "G V"
                          :command "g v"
                          :groups [:navigation]
                          :fn #(st/emit! (dw/go-to-viewer))}

   :open-handoff         {:tooltip "G H"
                          :command "g h"
                          :groups [:navigation]
                          :fn #(st/emit! (dw/go-to-viewer {:section :handoff}))}

   :open-comments        {:tooltip "G C"
                          :command "g c"
                          :groups [:navigation]
                          :fn #(st/emit! (dw/go-to-viewer {:section :comments}))}

   :open-dashboard       {:tooltip "G D"
                          :command "g d"
                          :groups [:navigation]
                          :fn #(st/emit! (dw/go-to-dashboard))}
   

   :toggle-scale-text   {:tooltip "K"
                         :command "k"
                         :fn #(st/emit! (toggle-layout-flag :scale-text))}

   :open-color-picker    {:tooltip "I"
                          :command "i"
                          :fn #(st/emit! (mdc/picker-for-selected-shape))}

   :bool-union           {:tooltip (ds/meta (ds/alt "U"))
                          :command (ds/c-mod "alt+u")
                          :fn #(st/emit! (dw/create-bool :union))}

   :bool-difference      {:tooltip (ds/meta (ds/alt "D"))
                          :command (ds/c-mod "alt+d")
                          :fn #(st/emit! (dw/create-bool :difference))}

   :bool-intersection    {:tooltip (ds/meta (ds/alt "I"))
                          :command (ds/c-mod "alt+i")
                          :fn #(st/emit! (dw/create-bool :intersection))}

   :bool-exclude         {:tooltip (ds/meta (ds/alt "E"))
                          :command (ds/c-mod "alt+e")
                          :fn #(st/emit! (dw/create-bool :exclude))}

   :artboard-selection   {:tooltip (ds/meta (ds/alt "G"))
                          :command (ds/c-mod "alt+g")
                          :fn #(st/emit! (dw/create-artboard-from-selection))}
   
   :toggle-focus-mode    {:command "f"
                          :tooltip "F"
                          :groups [:basic]
                          :fn #(st/emit! (dw/toggle-focus-mode))}

   :thumbnail-set {:tooltip (ds/shift "T")
                   :command "shift+t"
                   :fn #(st/emit! (dw/toggle-file-thumbnail-selected))}

   :show-pixel-grid      {:tooltip (ds/shift ",")
                          :command "shift+,"
                          :fn #(st/emit! (toggle-layout-flag :show-pixel-grid))}

   :snap-pixel-grid      {:command ","
                          :tooltip ","
                          :fn #(st/emit! (toggle-layout-flag :snap-pixel-grid))}
   :export-shapes     {:tooltip (ds/meta-shift "E")
                       :command (ds/c-mod "shift+e")
                       :groups [:basics]
                       :fn #(st/emit!
                             (de/show-workspace-export-dialog))}

   :toggle-snap-guide   {:tooltip (ds/meta-shift "G")
                         :command (ds/c-mod "shift+G")
                         :groups []
                         :fn #(st/emit! (toggle-layout-flag :snap-guides))}

   :show-tooltips     {:command (ds/meta-shift "?")
                       :tooltip (ds/c-mod "shift+?")
                       :fn #()}})

(def opacity-shortcuts
  (into {} (->>
            (range 10)
            (map (fn [n] [(keyword (str "opacity-" n))
                          {:tooltip (str n)
                           :command (str n)
                           :fn #(st/emit! (dwly/pressed-opacity n))}])))))

(def shortcuts
  (merge base-shortcuts opacity-shortcuts))

(defn get-tooltip [shortcut]
  (assert (contains? shortcuts shortcut) (str shortcut))
  (get-in shortcuts [shortcut :tooltip]))
