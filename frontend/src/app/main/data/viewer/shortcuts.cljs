;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) UXBOX Labs SL

(ns app.main.data.viewer.shortcuts
  (:require
   [app.main.data.shortcuts :as ds]
   [app.main.data.viewer :as dv]
   [app.main.store :as st]))

(def shortcuts
  {:increase-zoom      {:tooltip "+"
                        :command "+"
                        :groups [:zoom-viewer :zoom]
                        :fn (st/emitf dv/increase-zoom)}

   :decrease-zoom      {:tooltip "-"
                        :command "-"
                        :groups [:zoom-viewer :zoom]
                        :fn (st/emitf dv/decrease-zoom)}

   :select-all         {:tooltip (ds/meta "A")
                        :command (ds/c-mod "a")
                        :groups [:viewer]
                        :fn (st/emitf (dv/select-all))}

   :reset-zoom         {:tooltip (ds/shift "0")
                        :command "shift+0"
                        :groups [:zoom-viewer :zoom]
                        :fn (st/emitf dv/reset-zoom)}

   :toggle-zoom-style  {:tooltip "F"
                        :command "f"
                        :groups [:zoom-viewer :zoom]
                        :fn (st/emitf dv/toggle-zoom-style)}

   :toogle-fullscreen  {:tooltip (ds/shift "F")
                        :command "shift+f"
                        :groups [:zoom-viewer :zoom]
                        :fn (st/emitf dv/toggle-fullscreen)}

   :next-frame         {:tooltip ds/left-arrow
                        :command "left"
                        :groups [:viewer]
                        :fn (st/emitf dv/select-prev-frame)}

   :prev-frame         {:tooltip ds/right-arrow
                        :command "right"
                        :groups [:viewer]
                        :fn (st/emitf dv/select-next-frame)}

   :open-handoff       {:tooltip "G H"
                        :command "g h"
                        :groups [:nav-viewer :navigation]
                        :fn #(st/emit! (dv/go-to-section :handoff))}

   :open-comments      {:tooltip "G C"
                        :command "g c"
                        :groups [:nav-viewer :navigation]
                        :fn #(st/emit! (dv/go-to-section :comments))}

   :open-interactions  {:tooltip "G V"
                        :command "g v"
                        :groups [:nav-viewer :navigation]
                        :fn #(st/emit! (dv/go-to-section :interactions))}

   :open-workspace     {:tooltip "G W"
                        :command "g w"
                        :groups [:nav-viewer :navigation]
                        :fn #(st/emit! (dv/go-to-workspace))}})

(defn get-tooltip [shortcut]
  (assert (contains? shortcuts shortcut) (str shortcut))
  (get-in shortcuts [shortcut :tooltip]))
