(ns ^:figwheel-no-load owe.dev
  (:require
    [owe.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)
