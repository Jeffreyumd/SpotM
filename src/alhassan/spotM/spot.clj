(ns alhassan.spotM.spot
  (:import (com.wrapper.spotify SpotifyApi$Builder))
  (:require [clojure.java.data :refer [from-java]]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.xml :as xml]
            [clojure.string :as string]))

(defn make-client []
  (let [spotifyApi (-> (SpotifyApi$Builder.)
                     (.setClientId "33b1c0aaa872462eb101998593ecbbae")
                     (.setClientSecret "2fb7ec1e27534c4485ffb311508a3090")
                     (.build))
        accessToken (-> spotifyApi
                        (.clientCredentials)
                        (.build)
                        (.execute)
                        (.getAccessToken))
        _ (.setAccessToken spotifyApi accessToken)]
    spotifyApi))


(def client (make-client))

(defn search [query]
  (from-java
    (-> client (.searchAlbums "The beatles")
        (.build)
        (.execute))))

(defn load-crime-data []
  (let [csv-data->maps (fn [csv-data]
                         (map zipmap
                              (->> (first csv-data) ;; First row is the header
                                   (map keyword) ;; Drop if you want string keys instead
                                   repeat)
                              (rest csv-data)))]
    (with-open [reader (io/reader "crime_incident_reports.csv")]
      (doall
        (csv-data->maps (csv/read-csv reader))))))

(def crime-data (load-crime-data))

(def crime-sample (take 1000 crime-data))

(defn format-for-copy-paste-map [crime]
  (str
    (:Lat crime)
    " "
    (:Long crime)
    " "
    "cross2"
    " "
    "red"
    " "
    "1"
    " "
    (:OFFENSE_DESCRIPTION crime)))


(println
  (String/join "\n" (map format-for-copy-paste-map crime-sample)))



(defn elem [name attrs inner]
  (when (not (sequential? inner))
    (throw (IllegalArgumentException.
             (str "Inner needs to be a vector. Called with ["
                  (string/join ", " [name attrs inner])
                  "]"))))
  {:tag name
   :attrs attrs
   :content inner})


(defn crime-to-gmaps-marker [crime]
  (elem "Placemark" {}
        [(elem "name" {} [(:INCIDENT_NUMBER crime)])
         (elem "description" {} [(:OFFENSE_DESCRIPTION crime)])
         (elem "Point" {}
               [(elem "coordinates" {}
                      [(str (:Long crime)
                            ","
                            (:Lat crime)
                            ",0")])])]))

(defn crimes-to-kml [crimes]
  (elem "kml" {:xmlns "http://www.opengis.net/kml/2.2"}
    [(elem "Folder" {}
       (concat [{:tag "name"
                 :content ["Boston Crime Data"]}
                {:tag "visibility"
                 :content ["1"]}]
               (map crime-to-gmaps-marker crimes)))]))


(def sample-kml (crimes-to-kml crime-sample))

(spit "crime_map.kml" (with-out-str (xml/emit sample-kml)))

(def full-kml (crimes-to-kml crime-data))
(spit "full_crime_map.kml" (with-out-str (xml/emit full-kml)))
