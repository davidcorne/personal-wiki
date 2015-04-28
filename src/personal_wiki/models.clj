(ns personal-wiki.models
  (:require 
   [monger.core :as monger]
   )
  )

(defn initialise [uri]
  (println "Connecting to:" uri)
  (let [{:keys [connection db]} (monger/connect-via-uri uri)]
    (def db db)
    (def note-collection "notecollection")
    )
  )
