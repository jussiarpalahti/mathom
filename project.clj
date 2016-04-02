(defproject
  boot-project
  "0.0.0-SNAPSHOT"
  :dependencies
  [[org.clojure/clojure "1.7.0"]
   [org.clojure/clojurescript "1.7.170"]
   [adzerk/boot-cljs "1.7.170-3"]
   [pandeiro/boot-http "0.7.0"]
   [adzerk/boot-reload "0.4.2"]
   [adzerk/boot-cljs-repl "0.3.0"]
   [com.cemerick/piggieback "0.2.1"]
   [weasel "0.7.0"]
   [org.clojure/tools.nrepl "0.2.12"]
   [com.cognitect/transit-cljs "0.8.237"]
   [markdown-clj "0.9.85"]
   [binaryage/devtools "0.5.2"]]
  :source-paths
  ["src/cljs" "html"])