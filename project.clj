(defproject sporting-fixtures "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.csv "0.1.4"]
                 [org.clojure/data.xml "0.1.0-beta1"]
                 [org.clojure/data.json "2.4.0"]
                 [clojure.java-time "0.3.3"]
                 [clj-http "3.10.0"]
                 [clj-yaml "0.4.0"]
                 [clj-time "0.15.0"]
                 [cheshire "5.8.1"]        ;; Used for formatting JSON
                 [io.forward/yaml "1.0.9"] ;; Used for formatting YAML
                 [hickory "0.7.1"]         ;; Used for parsing HTML
                 [enlive "1.1.6"]          ;; Used for formatting/templating HTML (another library)
                 ]
  :test-paths ["test/src"]
  :main ^:skip-aot sporting-fixtures.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
