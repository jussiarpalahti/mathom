
(ns mathom.toolbar)

(def tool (atom {}))

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
                         (if v
                           (str (name k) "=\"" v "\"")))))


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

(defn create-tb
  []
  (let [body (aget (.getElementsByTagName js/document "body") 0)
        tb (.createElement js/document "div")]
    (.setAttribute tb "id" "mathom_toolbar")
    (.appendChild body tb)
    tb))

(defn setup
  []
  (swap! tool
         #(assoc @tool :bar (.getElementById js/document "mathom_toolbar"))))

(defn render
  [statedb]
  (let [states (:states statedb)
        active (:active_state statedb)
        prev (if (and active (> active 0)) true false)
        next (if (and active (< active (dec (count states)))) true false)]
    (set-content (.getElementById js/document "mathom_toolbar")
                 (h "div" {} [(h "h1" {} ["Mathom Toolbar"])
                              (h "button"
                                 {:id "mathom_toolbar_prev"
                                  :class "pure-button"
                                  :disabled (not prev)}
                                 ["&lt; &lt;"])
                              (h "button"
                                 {:id "mathom_toolbar_next"
                                  :class "pure-button"
                                  :disabled (not next)}
                                 ["&gt; &gt;"])
                              (h "span"
                                 {:id "mathom_toolbar_states"}
                                 ["Saved states: " (count states)
                                  (if active (clojure.string/join
                                               ["Active state: " (inc active)]))])
                              (h "button"
                                 {:id       "mathom_toolbar_clear"
                                  :class    "pure-button"
                                  :disabled (< (count states) 1)}
                                 ["Clear"])]))))


(defn ^:dynamic handle-event
  [eib]
  (println "not eventing"))

(defn attach-listener
  [fun]
  (.addEventListener (.getElementById js/document "mathom_toolbar")
                     "click"
                     #(fun (aget (.-target %) "id"))))
