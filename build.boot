(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}

 :dependencies '[[org.clojure/clojure "1.7.0"]         ;; add CLJ
                 [org.clojure/clojurescript "1.7.170"] ;; add CLJS
                 [adzerk/boot-cljs "1.7.170-3"]
                 [pandeiro/boot-http "0.7.0"]
                 [adzerk/boot-reload "0.4.2"]           ;; add boot-reload
                 [adzerk/boot-cljs-repl "0.3.0"]        ;; add bREPL
                 [com.cemerick/piggieback "0.2.1"]      ;; needed by bREPL
                 [weasel "0.7.0"]                       ;; needed by bREPL
                 [org.clojure/tools.nrepl "0.2.12"]     ;; needed by bREPL
                 [com.cognitect/transit-cljs "0.8.237"] ;; Transit for data
                 [markdown-clj "0.9.85"]                ;; Markdown for export
                 [binaryage/devtools "0.5.2"]           ;; For sanity
                ])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]] ;; make reload visible
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]) ;; make it visible

(deftask dev
  "The works"
  []
  (comp
    (serve :dir "target" :port 8080)
    (watch)
    (reload)
    (cljs-repl)
    (cljs :compiler-options {:parallel-build true})
    (target :dir #{"target"})))

; {:compiler-options {:parallel-build true}}

(defn- generate-lein-project-file! [& {:keys [keep-project] :or {:keep-project true}}]
  (require 'clojure.java.io)
  (let [pfile ((resolve 'clojure.java.io/file) "project.clj")
        ; Only works when pom options are set using task-options!
        {:keys [project version]} (:task-options (meta #'boot.task.built-in/pom))
        prop #(when-let [x (get-env %2)] [%1 x])
        head (list* 'defproject (or project 'boot-project) (or version "0.0.0-SNAPSHOT")
               (concat
                 (prop :url :url)
                 (prop :license :license)
                 (prop :description :description)
                 [:dependencies (get-env :dependencies)
                  :source-paths (vec (concat (get-env :source-paths)
                                             (get-env :resource-paths)))]))
        proj (pp-str head)]
      (if-not keep-project (.deleteOnExit pfile))
      (spit pfile proj)))

(deftask lein-generate
  "Generate a leiningen `project.clj` file.
   This task generates a leiningen `project.clj` file based on the boot
   environment configuration, including project name and version (generated
   if not present), dependencies, and source paths. Additional keys may be added
   to the generated `project.clj` file by specifying a `:lein` key in the boot
   environment whose value is a map of keys-value pairs to add to `project.clj`."
 []
 (generate-lein-project-file! :keep-project true))
