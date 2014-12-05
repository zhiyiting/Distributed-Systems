CC=gcc
MPICC=mpicc
CFLAGS=-std=c99

LDFLAGS=
POINTSOURCES= \
	point/point_clustering_sequential.c \
	point/kmeans.c

POINTMPI= \
	point/point_clustering_parallel.c \
	point/kmeans.c

DNASOURCES= \
	dna/dna_clustering_sequential.c \
	dna/kmeans.c

DNAMPI= \
	dna/dna_clustering_parallel.c \
	dna/kmeans.c

MACHINEFILE=machinefile

POINTOBJECTS=$(POINTSOURCES:.cpp=.o)

POINTMPIOBJ=$(POINTMPI:.cpp=.o)

DNAOBJECTS=$(DNASOURCES:.cpp=.o)

DNAMPIOBJ=$(DNAMPI:.cpp=.o)

POINTEXECUTABLE=point/seq

POINTMPIEXE=point/par

DNAEXECUTABLE=dna/seq

DNAMPIEXE=dna/par

all: \
	$(POINTSOURCES) $(POINTEXECUTABLE) \
	$(DNASOURCES) $(DNAEXECUTABLE) \
	$(POINTMPI) $(POINTMPIEXE) \
	$(DNAMPI) $(DNAMPIEXE)

$(POINTEXECUTABLE): $(POINTOBJECTS)
	$(CC) $(CFLAGS) $(LDFLAGS) $(POINTOBJECTS) -o $@

$(POINTMPIEXE): $(POINTMPIOBJ)
	$(MPICC) $(CFLAGS) $(LDFLAGS) $(POINTMPIOBJ) -o $@

$(DNAEXECUTABLE): $(DNAOBJECTS)
	$(CC) $(CFLAGS) $(LDFLAGS) $(DNAOBJECTS) -o $@

$(DNAMPIEXE): $(DNAMPIOBJ)
	$(MPICC) $(CFLAGS) $(LDFLAGS) $(DNAMPIOBJ) -o $@

.cpp.o:
	$(CC) $(CFLAGS) $< -o $@

clean:
	rm -rf $(POINTEXECUTABLE) $(DNAEXECUTABLE) $(POINTMPIEXE) $(DNAMPIEXE)

runpar3:
	mpirun -np 3 -machinefile machinefile par