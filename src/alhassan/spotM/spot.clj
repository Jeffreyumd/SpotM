(ns alhassan.spotM.spot
  (:require [clojure.java.data :refer [from-java]]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.xml :as xml]
            [clojure.string :as string])
  (:gen-class))




(defn -main [args])




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



(defn elem
  " helper to creates XML elements"
  [name attrs inner]
  (when (not (sequential? inner))
    (throw (IllegalArgumentException.
             (str "Inner needs to be a vector. Called with ["
                  (string/join ", " [name attrs inner])
                  "]"))))
  {:tag name
   :attrs attrs
   :content inner})


(defn crime-to-gmaps-marker
  "create the XML for marker in a google map representing a crime"
  [crime]
  (elem "Placemark" {}
        [(elem "name" {} [(:INCIDENT_NUMBER crime)])
         (elem "description" {} [(:OFFENSE_DESCRIPTION crime)])
         (elem "Point" {}
               [(elem "coordinates" {}
                      [(str (:Long crime)
                            ","
                            (:Lat crime)
                            ",0")])])]))

(defn crimes-to-kml
  " convert crimes into a XML representation to an KML file for google maps "
  [crimes]
  (elem "kml" {:xmlns "http://www.opengis.net/kml/2.2"}
    [(elem "Folder" {}
       (concat [{:tag "name"
                 :content ["Boston Crime Data"]}
                {:tag "visibility"
                 :content ["1"]}]
               (map crime-to-gmaps-marker crimes)))]))


;; load crime data
(def sample-kml (crimes-to-kml crime-sample))
;; write to crime_map.kml
(spit "crime_map.kml" (with-out-str (xml/emit sample-kml)))

(def full-kml (crimes-to-kml crime-data))
(spit "full_crime_map.kml" (with-out-str (xml/emit full-kml)))
