Experimental playground for CUDA and DOD
========================================

DOD links

* [Links](http://technbolts.tumblr.com/post/7702231622)

CUDA and Java binding

* [JCuda](http://www.jcuda.de/jcuda/JCuda.html)
* [Cuda](http://developer.nvidia.com/cuda-toolkit-40)

## Project layout

* 'data/cuda' contains cuda source file (*.cu) and their compiled version (*.ptx)
* 'lib/' used for both sbt library management and in the native library lookup (modified version of 'cuda.LibUtils' to define the base lib dir.)