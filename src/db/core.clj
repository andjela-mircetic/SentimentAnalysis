(ns db.core
  (:require [monger.core :as mg]
            [monger.collection :as mc]))

(defonce conn (mg/connect))
(defonce db (mg/get-db conn "ChatApp"))

(defn get-db []
   db)

(defn insert [coll data]
  (mc/insert (get-db) coll data))

(defn find-maps [coll query]
  (mc/find-maps (get-db) coll query))

(defn find-one-as-map [coll query]
  (mc/find-one-as-map (get-db) coll query)) 

(defn update [coll query update-ops]
  (mc/update (get-db) coll query update-ops))