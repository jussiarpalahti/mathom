
(ns mathom.toolbar)

(def tool (atom {}))

(defn clean
  []
  (let [prev (.getElementById js/document "mathom_toolbar")]
    (if (not (nil? prev))
      (.remove prev))))

(defn set-content
  [content]
  (set! (.-innerHTML (:bar @tool)) content))

(def content "
<div><h1>Mithril ClojureScript Toolbar</h1>
<div>
<button id=\"mathom_toolbar_prev\">Previous state</button>
<button id=\"mathom_toolbar_next\">Next state</button>
<div id=\"mathom_toolbar_states\"></div>
</div>")

(defn setup
  []
  (let [body (aget (.getElementsByTagName js/document "body") 0)
        tb (.createElement js/document "div")]
    (clean)
    (.setAttribute tb "id" "mathom_toolbar")
    (.appendChild body tb)
    (set-content content)
    (swap! tool #(assoc @tool :bar tb)) ; To get access to generated node
    ))

(defn handle-event
  [eib])

(defn attach-listener
  []
  (.addEventListener (:bar @tool)
                     "click"
                     #(handle-event (aget (.-target %) "id"))))
