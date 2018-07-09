(ns owe.core
  (:require
    [ajax.core :refer [GET]]
    cljsjs.react-chartjs-2
    [clojure.string :as string]
    [goog.object :as obj]
    [reagent.core :as r]))


(defn value-of [e]
  (-> e .-target .-value))

(defn get-stats [opts stats error?]
  (reset! error? false)
  (GET (str "/api/"
            (:platform opts) "/"
            (string/replace (:id opts) #"#" "-"))
       {:response-format :json
        :keywords?       true
        :handler         (fn [result]
                           (reset! stats result))
        :error-handler   #(reset! error? true)}))

(def doughnut-chart (r/adapt-react-class (obj/get js/ReactChartjs2 "Doughnut")))

(defn endorsement-chart [stats]
  (let [{:keys [shotcaller teammate sportsmanship]} @stats]
    [doughnut-chart {:data {:labels   ["Shotcaller" "Teammate" "Sportsmanship"]
                            :datasets [{:data            [shotcaller teammate sportsmanship]
                                        :backgroundColor ["#f29413" "#c81ef5" "#40ce43"]}]}}]))

(defn page []
  (r/with-let [opts   (r/atom {:platform "pc"})
               stats  (r/atom nil)
               error? (r/atom false)]
    [:div
     [:h2 "Overwatch Endorsement Stats"]
     [:div.pure-form
      [:select#platform
       {:style     {:margin-right "5px"}
        :on-change #(swap! opts assoc :platform (value-of %))}
       [:option {:value "pc"} "PC"]
       [:option {:value "xbl"} "Xbox Live"]
       [:option {:value "psn"} "PSN"]]
      [:input
       {:type        :text
        :style       {:margin-right "5px"}
        :placeholder "BattleTag"
        :value       (:id @opts)
        :on-change   #(swap! opts assoc :id (value-of %))
        :on-key-down #(if (= 13 (.-which %))
                        (get-stats @opts stats error?))}]
      [:button.pure-button.pure-button-primary
       {:style    {:margin-right "5px"}
        :on-click #(get-stats @opts stats error?)}
       "Submit"]]
     (if @error?
       [:div.error "An error occurred while processing your request. Please try again."])
     (if @stats
       [endorsement-chart stats])]))


;; https://purecss.io/
;; https://gist.github.com/edwthomas/b1f653405e2827357133f7e3c245bea0
;; http://www.chartjs.org/samples/latest/charts/doughnut.html
;; http://www.chartjs.org/docs/latest/charts/doughnut.html
;; https://stackoverflow.com/questions/20966817/how-to-add-text-inside-the-doughnut-chart-using-chart-js


(defn init! []
  (r/render [page] (.getElementById js/document "app")))
