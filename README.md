Life Simulator
==============

This is a project I've done previously for research project. The simulator aims to simulate simplified ecosystem, with an idea of using genetics algorithm.
Programming and researching was finished at the year of 2013, and now sharing the code of it.

Goals
-----

* Simulate ecosystem within a limited environment
* Make a simulation reproducible 

Things have done
----------------

* Physics calculation with JBox2D
* Each life unit(refered as 'Lifeform') has its own gene of following factors, being (inverse) propotional to some of the others:
  * Size
  * Bounciness
  * Moving speed
  * Breeding speed
  * Amount of energy consumed by metabolism
  * Maximum amount of energy the lifeform can have
  * Power applying to the other, colliding lifeform
  * Range of sight
  * Frequency of movement
  * Maximum lifespan, if not died by the lack of energy
  * Frequency of breeding
  * Color, used as a division of species
* Each factors are related to some other factors, so is propotional or inverse- to the related factors. - : propotional, ~: inverse propotional
  * Size ~ Bounciness
  * Speed - Breeding speed - Metabolism energy
  * Maximum energy ~ Power
  * Sight ~ Movement frequency
  * Lifespan ~ Breeding frequency
* The gene can be crossed over and mutated. The probability of mutation can be set through configuration.
* Lifeforms with close color are considered as a same species and can breed each other. Otherwise when they collide, they damages and gains(or lose) energy.
  * The color also is a target of gene crossover and mutation. Continued inbreeding can make species' color uniform. Sometimes a new species can occur.

* Simulation progress is shown in real-time. The speed of simulation is limited to the on-screen framerate.
  * The default framerate is 60, and can be changed lower if performance suffers.
  * Framerate can even be made to zero, making the simulation to be done with maximum speed possible. The screen doesn't show up, so the video recording might be needed to see the progress.
* The progress of a simulation can be recorded to a video, with the help of [Xuggler](http://www.xuggle.com/xuggler/)
* A simulation can be reproduced with the seed shown in configuration panel. The seed is also written on a file, if the simulation is recorded.


Things to do
------------

* Add some comments
* Generalize the interface of the simulator classes.
* Add test units
* Update physics engine, or make one, to resolve bottleneck on physics calculation. Possible candidate for replacement: [LiquidFun](https://github.com/google/liquidfun/), Native C++ version of Box2D
