(ns mathom.m)

;
; Mithril helpers
;

(defn m [tag attrs values]
  "Mithril in Cljs"
  (js/m tag (clj->js attrs) (clj->js values)))

(defn nm
  "Mithril in Cljs supporting different call methods"
  ([tag]
   (m tag nil nil))
  ([tag par2]
   (if (= (type par2) (type []))
     (m tag nil par2)
     (if (= (type par2) (type {}))
       (m tag par2 nil)
       (m tag nil par2))))
  ([tag par2 par3]
   (m tag par2 par3)))

(defn route_param [param]
  "Get param from route params"
  ((aget (.-route js/m) "param") param))

(defn request
  "m.request wrapper with promise handling"
  [options cb & err]
  (.then (.request js/m (clj->js options))
         (fn [resp] (cb (js->clj resp)))
         (if (not err) #(.log js/console %) err)))

;
; Closure helpers
;

(defn format_time [d format]
  "Render instance of js/Date according to format"
  (let [format (new goog.i18n.DateTimeFormat format)]
    (.format format d)))
