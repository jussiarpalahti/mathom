
(ns mathom.core
  (:require
            [m]
            [devtools.core :as devtools]
            [cljs.reader :refer [read-string]]
            [cljs.js :refer [empty-state eval js-eval]]
            [cljs.pprint :refer [pprint]]
            [mathom.m :refer [nm request route_param text]]
            [mathom.toolbar]
            ))

;
; ClojureScript helpers
;

(enable-console-print!)

(devtools/set-pref! :install-sanity-hints true)
(devtools/install!)

;
; App dev helpers
;

; Tooldb is for Mithril runtime tools like state store
(def tooldb (atom {:active_state nil :states []}))

(defn save-route
  "Persist route into state db
  under cleverly hidden ::meta structure"
  [state]
  (assoc state ::meta {:route (.route js/m)}))

(defn save-state
  "Saves state into tooldb :states array and sets
  active_state point to last item of array"
  [state]
  (swap! tooldb update-in [:states] #(conj % (save-route state)))
  (swap! tooldb update-in [:active_state] #(dec (count (:states @tooldb)))))

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

(defn serialize-local-js
  "Serializes given data to localstorage under given key
  assuming that data is convertable to JS"
  [key data]
  (set-item! key (.stringify js/JSON (clj->js data))))

(defn deserialize-local-js
  "Returns Cljs datastructure from localstorage
  for given key"
  [key]
  (let [s (get-item key)]
    (if (nil? key)
      nil
      (js->clj (.parse js/JSON (get-item key))))))

(defn serialize-local
  [key data]
  (set-item! key (serialize-edn data)))

(defn deserialize-local
  [key]
  (let [s (get-item key)]
    (if (nil? s)
      nil
      (eval-str s))))

;;
;; Sample App
;;

(defn initialize-db
  "Creates closure for app database
  from given data as base map"
  [data]
  (let [cdb (atom data)]
    {:update  (fn [key val]
                (swap! cdb #(assoc % key val))
                (save-state @cdb))
     :query   (fn [key]
                (get @cdb key))
     :persist (fn [] (serialize-local "data" (save-route @cdb)))}))

(defn index_view
  [{:keys [update query persist]} ctrl]
  (let [username (query :username)]
    (nm "div"
      [(nm "h1" "Mithril app with data persistence")
       (nm "div" username)
       (nm "div" [(text "username" username 40 #(update :username %))
                  (nm "button.pure-button" {:onclick #(persist)} "Persist")])
       (nm "div" [(nm "textarea" {"rows" 10 "cols" 40} (get-item "data"))])])))

(def index-app-db
  (let [db (deserialize-local "data")]
    (if (nil? db)
      (initialize-db {:username "jussiarpalahti"})
      (initialize-db db))))

(def index {:controller (fn [] index-app-db)
            :view index_view})

;; Routing mode
(aset (.-route js/m) "mode" "hash")

(defn setup []
  "Mount app"
  (do
    (.route js/m
            (.getElementById js/document "app")
            "/"
            (clj->js {"/" index}))))

(setup)
(.route js/m "/")

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
; Remember: 9 is for ending (I guess), not 8 for starting the wrap
; Kill is the regular C-k, it works from cursor to form end, which is ), ], " or }
; Raise actually works now, because Cmd-' is seen by IntelliJ as Cmd-\ for some reason...
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
