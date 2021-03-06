
(ns mathom.core
  (:require
            [m]
            [devtools.core :as devtools]
            [cljs.reader :refer [read-string]]
            [cljs.js :refer [empty-state eval js-eval]]
            [cljs.pprint :refer [pprint]]
            [mathom.m :refer [nm request route_param text]]
            [mathom.toolbar :as toolbar]
            ))

;
; ClojureScript helpers
;

(enable-console-print!)

(devtools/set-pref! :install-sanity-hints true)
(devtools/install!)

;
; App database
;

(def appdb (atom {}))

;
; App dev helpers
;

; Tooldb is for Mithril runtime tools like state store
(def tooldb (atom {:active_state nil :states []}))
(declare serialize-local)

(defn slice-vec
  "Get a subvec of given vec
  from start to cursor or end
  of vec if cursor is nil
  with item added to the end"
  [states state active]
  (let [cursor (if (nil? active) (count states) (inc active))]
    (conj (subvec states 0 cursor) state)))

(defn save-route
  "Persist route into state db
  under cleverly hidden ::meta structure"
  [state]
  (assoc state ::meta {:route (.route js/m)}))

(defn save-state
  "Saves state into tooldb :states array and
  local storage under \"tooldb\".
  Sets active_state point to last item of array"
  [state]
  (swap! tooldb update-in [:states]
         #(slice-vec % (save-route state) (:active_state @tooldb)))
  (swap! tooldb update-in [:active_state] #(dec (count (:states @tooldb))))
  (serialize-local "tooldb" @tooldb)
  (toolbar/render @tooldb))

(defn set-app-state
  [state]
  (reset! appdb state)
  (let [stored_route (get-in state [::meta :route])]
    (if (= (.route js/m) stored_route)
      (.redraw js/m)
      (.route js/m stored_route))))

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

(defn prev-state
  []
  (swap! tooldb update-in [:active_state] #(dec (:active_state @tooldb)))
  (set-app-state (get (:states @tooldb) (:active_state @tooldb)))
  (toolbar/render @tooldb))

(defn next-state
  []
  (swap! tooldb update-in [:active_state] #(inc (:active_state @tooldb)))
  (set-app-state (get (:states @tooldb) (:active_state @tooldb)))
  (toolbar/render @tooldb))

(defn clear-states
  "Empty app state from local storage
  and app databases"
  []
  (reset! appdb {})
  (reset! tooldb {:active_state nil :states []})
  (remove-item! "data")
  (remove-item! "tooldb")
  (toolbar/render @tooldb)
  (.redraw js/m))

(defn rewind
  [eid]
  (case eid
    "mathom_toolbar_prev" (prev-state)
    "mathom_toolbar_next" (next-state)
    "mathom_toolbar_clear" (clear-states)))

(defn setup-toolbar
  []
  (let [tdb (deserialize-local "tooldb")]
    (if (not (nil? tdb))
      (reset! tooldb tdb))
    (toolbar/render tdb)
    (toolbar/attach-listener rewind)))


;;
;; Sample App
;;

(defn initialize-db
  "Creates closure for app database
  from given data as base map"
  [data]
  (reset! appdb data)
  {:update  (fn [key val]
              (swap! appdb #(assoc % key val))
              (save-state @appdb))
   :query   (fn [key]
              (get @appdb key))
   :persist (fn [] (serialize-local "data" (save-route @appdb)))})

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
;(.route js/m "/")

(setup-toolbar)

;
; First state should go to saved states
; TODO: Check init logic and fix this hack...
;
(if (empty? (:states @tooldb))
  (save-state @appdb))

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
