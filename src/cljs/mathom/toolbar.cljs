
(ns mathom.toolbar)

(def tool (atom {:selected nil}))

(defn clean
  []
  (let [prev (.getElementById js/document "mathom_toolbar")]
    (if (not (nil? prev))
      (.remove prev))))

(defn set-content
  [elem content]
  (set! (.-innerHTML elem) content))

(def content "
<div><h1>Mithril ClojureScript Toolbar</h1>
<div>
<button id=\"mathom_toolbar_prev\">Previous state</button>
<button id=\"mathom_toolbar_next\">Next state</button>
<div id=\"mathom_toolbar_states\"></div>
</div>")

(defn d-to-s
  "Returns stringified version
  of given map in HTML/XML
  attribute style"
  [d]
  (clojure.string/join " "
                       (for [[k v] d]
                         (str (name k) "=\"" v "\""))))


(defn h
  "Returns a string resembling
  HTML tag, attributes and children"
  [tag attrs children]
  (str "<" tag
       (if (not(empty? attrs)) (str " " (d-to-s attrs))) ">"
       (if (not(empty? children)) (clojure.string/join " " children))
       "</" tag ">"))


(defn esc-s
  "Escape things bad for HTML"
  [s]
  (clojure.string/escape s
                         {\< "&lt;", \> "&gt;", \& "&amp;"}))

(defn setup
  []
  (let [body (aget (.getElementsByTagName js/document "body") 0)
        tb (.createElement js/document "div")]
    (clean)
    (.setAttribute tb "id" "mathom_toolbar")
    (.appendChild body tb)
    (swap! tool #(assoc @tool :bar tb)))) ; To get access to generated node

(defn render
  [statedb]
  (let [states (:states statedb)
        active (:active_state statedb)]
    (set-content (:bar @tool)
                 (h "div" {} [(h "h1" {} ["Mathom Toolbar"])
                              (h "button" {:id "mathom_toolbar_prev"} ["Previous state"])
                              (h "button" {:id "mathom_toolbar_next"} ["Next state"])
                              (h "span"
                                 {:id "mathom_toolbar_states"}
                                 ["Saved states: " (count states)
                                  (if active (clojure.string/join
                                               ["Active state: " (inc active)]))])]))))


(defn ^:dynamic handle-event
  [eib])

(defn attach-listener
  []
  (.addEventListener (:bar @tool)
                     "click"
                     #(handle-event (aget (.-target %) "id"))))
