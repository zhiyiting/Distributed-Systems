Lab 1: Portable, Migratable Work

One classic problem in distributed systems involves the migration of work. This problem is often referred to as that of Process Migration. But, the vehicle might be processes, objects, threads, or any other unit of active work. The common idea is moving in-progress work with as little disruption and 
wastage as possible. 

To make this possible, the work-in-progress should be agnostic to what node it is running on and should be unaware of whether it has been moved from one node to another. Thus, it can enjoy the illusion that it is running on a single node during its entire execution. 

To achieve this, it is necessary to be able to pause and package up a process, ship it to another node, and unpackage and resume it such that it is running again. The process should not lose it’s location in the program, variables, open files, network connections or any other state.
