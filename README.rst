==============
Play'R - Mongo
==============

Add MongoDb support to Play'R.


This is an extension to the `Play'R project <https://github.com/26lights/PlayR>`_ 

.. warning::


How to use it
=============


First, you have to add ``playr-mongo`` to your build dependencies ( ``build.sbt`` ):

.. code-block:: scala

  resolvers += "26Lights snapshots" at "http://build.26source.org/nexus/content/repositories/public-snapshots"

  libraryDependencies += "26lights"  %% "playr-mongo"  % "0.4.0-SNAPSHOT"



