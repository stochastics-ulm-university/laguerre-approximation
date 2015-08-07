Laguerre tessellation approximation of 3D images
================================================

This software packages provides an implementation of the algorithm proposed
in [1]. It aims to detect a Laguerre tessellation that approximates a given
grain structure as well as possible. An interface-based discrepancy measure
is minimized using the cross-entropy method, a robust stochastic optimization
technique.

> [1] A. Spettl, T. Brereton, Q. Duan, T. Werz, C.E. Krill III, D.P. Kroese
> and V. Schmidt, Fitting Laguerre tessellation approximations to tomographic
> image data. [arXiv:1508.01341 [cond-mat.mtrl-sci]](http://arxiv.org/abs/1508.01341)

If you use this software package in a work that leads to a scientific paper,
we ask you to mention it explicitly and cite reference [1].


Usage
-----

This project requires Java 7 or newer. To compute suitable generators of
a Laguerre tessellation for a labeled 3D image "artificial_pcm_input.tif"
(located in the subfolder "data"), run:

    java -Xmx3500m -jar LaguerreApproximation3D.jar data/artificial_pcm_input.tif data/artificial_pcm_output.txt

In this example, the detected generators will be written to the text file
"artificial_pcm_output.txt". A Java heap size of 3500 MB is used, which is
necessary in order to process the example data set. The 3D image itself is
a labeled image, i.e., label zero stands for background, label 1 for the
first grain, label 2 for the second grain, etc. The grain regions should be
roughly convex. Furthermore, it is expected that there is at most a thin
layer of background voxels (1 voxel thickness) between adjacent grains. A
multi-page image file is required as input, i.e., a single file contains
several 2D images which correspond to the slices (in z-direction) of the
3D image. For example, [ImageJ](http://imagej.nih.gov/ij/) / [Fiji](http://fiji.sc/)
can generate such 3D image files. The generators text file that is created
contains the detected generators. One line corresponds to one generator.
The first value is the label, the remaining four values are x-, y-, z-coordinates
and the radius.

Note:

1. The executable JAR file "LaguerreApproximation3D.jar" is contained
   only in the binary release. If you want to compile the source code yourself:
   the class "laguerre_approximation_3d.LaguerreApproximation3D" contains the
   main method.
2. The experimental data set requires about 4500 MB Java heap (due to the
   cylindrical shape of the sample).

The following optional arguments may be used. Here, they are given with
their default values:

    --M=4000 --rho=0.05 --tauInject=10 --deltaInject=0.05 --injections=-1
    --gamma=0.9 --kappa=0.25 --tauTerminate=10 --deltaTerminate=0.01

The parameters are explained in [1]. The only exception is the `--injections`
parameter, which allows you to choose the number of variance injections manually.
The default value "-1" stands for the standard behavior, i.e., automatic
selection of the number of variance injections. If a value equal to or greater
than zero is specified, infinity is assumed as a new default value for `--gamma`
(in order to not abort variance injections early).


Source and Javadoc
------------------

The source code is supplied in the folder "src" and it contains Javadoc comments.
The main class is "laguerre_approximation_3d.LaguerreApproximation3D".


Copyright and licenses
----------------------

* Copyright &copy; 2015 Institute of Stochastics, Ulm University

This software is licensed under the GNU General Public License v2.0 (published
in June 1991), or (at your option) any later version. The full text of GPL 2 is
available at https://www.gnu.org/licenses/gpl-2.0 and in the file "LICENSE".


Changelog
---------

* 2015-08-06 - Initial release.


More info
---------

* http://arxiv.org/abs/1508.01341
* https://github.com/stochastics-ulm-university/laguerre-approximation
