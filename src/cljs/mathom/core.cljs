
(ns mathom.core
  (:require
            [m]
            [devtools.core :as devtools]
            [cljs.reader :refer [read-string]]
            [cljs.js :refer [empty-state eval js-eval]]
            [cljs.pprint :refer [pprint]]
            ))

(enable-console-print!)

(devtools/set-pref! :install-sanity-hints true)
(devtools/install!)

(defn serialize-edn
  "Serialize given data structure into string"
  [data]
  (with-out-str (cljs.pprint/pprint data)))

(defn eval-str [s]
  "Evaluate given string to Cljs data structure"
  (eval (empty-state)
        (read-string s)
        {:eval       js-eval
         :source-map true
         :context    :expr}
        (fn [result] result)))

(defn set-item!
  "Set `key' in browser's localStorage to `val`."
  [key val]
  (.setItem (.-localStorage js/window) key val))

(defn get-item
  "Returns value of `key' from browser's localStorage."
  [key]
  (.getItem (.-localStorage js/window) key))

(defn remove-item!
  "Remove the browser's localStorage value for the given `key`"
  [key]
  (.removeItem (.-localStorage js/window) key))

(defn serialize-local
  "Serializes given data to localstorage under given key
  assuming that data is convertable to JS"
  [key data]
  (set-item! key (.stringify js/JSON (clj->js data))))

(defn deserialize-local
  "Returns Cljs datastructure from localstorage
  for given key"
  [key]
  (js->clj (.parse js/JSON (get-item key))))


; Paredit mnemonics
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; all commands with C-M
; d goes down forward
; its reverse is u
; b goes down backward
; its reverse is n
; or to put it other way d and u go from position inward right or outward left
; b and n goes inward left and outward right
; barf and slurp go backwards with ctrl and forwards with shift j and k
; splice, split, raise and join are M-s M-S-s Cmd-' and ^-Cmd-s
; movement and selection is through various arrows and M and/or S
; Wrappers now work with Cmd + whatever is necessary to add the closing wrapping
; Like Cmd-s-9 when s-9 is ), or Cmd-M-9 for ] and Cmd-M-S-9 for }
; Kill is the regular C-k, it works from cursor to form end, which is ), ], " or }
; Raise actually works now, because Cmd-' is seen by IntelliJ as Cmd-\ for some reason...
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
