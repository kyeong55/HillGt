# HillGt

**HillGt — A distant glancing**

HillGt is an app that lubricates a communication between smartphone users.
In usual conversation scenario, we commonly conduct certain attention getting behaviors
before starting an actual conversation, i.e. tapping someone's shoulder, calling his/her
name, or staring at someone hoping he/she notice you have something to speak. HillGt 
tried to transplant these behaviors to the smartphone.

The name *HillGt* is originated '힐끗' a Korean word that denotes *glancing*.

## How it works

HillGt-er clicks the id of a person he/she want to talk with. `HillGt` generates a 
notification on a HillGt-ee's phone, and investigate user's reaction for about
7 seconds. Then `HillGt` determines HillGt-ee's attention level return it to
HillGt-er's phone. Now HillGt-er can know whether HillGt-ee is available / might
available / not available.

## Architecture

HillGt is built on top of two android services: `NunchiBab` and `Nunchi`.
`NunchitBab` is a data collection module that collect data from smartphone sensors
and convert them to fit `Nunchi` service. `Nunchi` is an insight generation module that
determine how attentive current smartphone user is.

To see more detailed implementation of each modules, visit respective repositories:

* [NunchitBab](https://github.com/kyeong55/NunchitBab)
* Nunchi

## References

These are the materials used for demo presentation conducted in class.

* [Presentation File](/docs/presentation.pdf)
* [Explanation Video](/docs/explanation.mov)
* [Demo Video](/docs/demo.mp4)
