import sys
import csv
import numpy
import getopt
import math
import random

def usage():
	print '$> python generatednastrand.py <required args> [optional args]\n' + \
        '\t-c <#>\t\tNumber of clusters to generate\n' + \
        '\t-p <#>\t\tNumber of points per cluster\n' + \
        '\t-o <file>\tFilename for the output of the dna strand data\n' + \
        '\t-v [#]\t\tLength of each dna strand\n'

def euclideanDistance(p1, p2):
    '''
    Takes two dna strands and computes the similarity between them.
    '''
    return sum(r1 != r2 for r1, r2 in zip(p1, p2))

def tooClose(point, points, minDist):
    '''
    Computes the similarity between the dna strand and all dna strands
    in the list, and if any points in the list are closer than minDist,
    this method returns true.
    '''
    for pair in points:
        if euclideanDistance(point, pair) < minDist:
                return True

    return False

def handleArgs(args):
    # set up return values
    numClusters = -1
    numStrands = -1
    output = None
    dnaLength = 20

    try:
        optlist, args = getopt.getopt(args[1:], 'c:p:v:o:')
    except getopt.GetoptError, err:
        print str(err)
        usage()
        sys.exit(2)

    for key, val in optlist:
        # first, the required arguments
        if   key == '-c':
            numClusters = int(val)
        elif key == '-p':
            numStrands = int(val)
        elif key == '-o':
            output = val
        # now, the optional argument
        elif key == '-v':
            dnaLength = int(val)

    # check required arguments were inputted  
    if numClusters < 0 or numStrands < 0 or \
            dnaLength < 1 or \
            output is None:
        usage()
        sys.exit()
    return (numClusters, numStrands, output, \
            dnaLength)

def drawOrigin(dnaLength):
    base = ('A', 'C', 'G', 'T')
    return [random.choice(base) for _ in range(0, dnaLength)]


# start by reading the command line
numClusters, \
numStrands, \
output, \
dnaLength = handleArgs(sys.argv)

writer = csv.writer(open(output, "w"))

# step 1: generate each strand centroid
centroids_strands = []
minDistance = 0
for i in range(0, numClusters):
    centroid_strand = drawOrigin(dnaLength)
    # is it far enough from the others?
    while (tooClose(centroid_strand, centroids_strands, minDistance)):
        centroid_strand = drawOrigin(dnaLength)
    centroids_strands.append(centroid_strand)

# step 2: generate the strands for each centroid
points = []
minClusterVar = 1
maxClusterVar = 5
base = ('A', 'C', 'G', 'T')
for i in range(0, numClusters):
    # compute the variance for this cluster
    variance = random.randint(minClusterVar, maxClusterVar)
    cluster = centroids_strands[i]
    for j in range(0, numStrands):
        # generate a dna strand with specified variance
        strand = list(cluster)
        for _ in range(1, variance + 1):
            index = random.randint(0, dnaLength - 1)
            strand[index] = random.choice(base)
        # write the points out
        writer.writerow(strand)