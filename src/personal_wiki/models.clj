(ns personal-wiki.models
  (:require 
   [monger.core :as monger]
   )
  )

(defn initialise []
  (def connection (monger/connect))
  (def db (monger/get-db connection "nodetest1"))
  (def note-collection "notecollection")
  )
