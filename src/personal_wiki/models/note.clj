(ns personal-wiki.models.note
  (:require 
   [monger.core :as mg]
   [monger.collection :as mc]
   [personal-wiki.models :as models]
   )
  (:import [org.bson.types ObjectId]))

;==============================================================================
(defn get-note [title]
  (->
   (mc/find-maps models/db models/note-collection {:title title})
   ; get the first member in the collection
   ;TODO make sure it has exactly one
   (first)
   ))

;==============================================================================
(defn get-notes []
  (mc/find-maps models/db models/note-collection {})
  )

;==============================================================================
(defn add! [note]
  (let [final (assoc note :_id (ObjectId.))]
    (println final)
    (mc/insert models/db models/note-collection final)
    ))

;==============================================================================
(defn update! [note]
  (let [old-note (get-note (get note :title))]
    (mc/update-by-id models/db models/note-collection (get old-note :_id) note)
    )
  )

;==============================================================================
(defn remove! [title]
  (mc/remove models/db models/note-collection {:title title})
  )
