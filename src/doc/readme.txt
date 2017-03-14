Commit 1 = (A,B,C,D)

TaskDependency ("dependency" table in DB)

[matrix below is a set of 4 dependencysets]

   A  B  C  D
A  x  x  x  x --> new DependencySet (A, dependenciesMap [A,1][B,1][C,1][D,1])
B     x  x  x --> new DependencySet (B, dependenciesMap [B,1][C,1][D,1])
C        x  x --> new DependencySet (C, dependenciesMap [C,1][D,1])
D           x --> new DependencySet (D, dependenciesMap [D,1])

taskDependency.setDependencies(matrix)

rawDependency: [client = filedependencyobject, supplier = filedependencyobject, label = commit]

--------------------------------------------------------------------------------

             A  B  C  D
Worker w1    x  x        --> new DependencySet (w1, dependenciesMap [A,1][B,1])            
Worker w2          x     --> new DependencySet (w2, dependenciesMap [C,1])            
Worker w3    x        x  --> new DependencySet (w3, dependenciesMap [A,1][D,1])

rawDependency: [client = filedependencyobject, supplier = authordependencyobject, label = commit]

--------------------------------------------------------------------------------