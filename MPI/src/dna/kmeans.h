#ifndef _KMEANS_H_
#define _KMEANS_H_

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include <time.h>

/* threshold to terminate kmeans */
#define THRESHOLD 4

/* config file structure */
typedef struct {
	int k, count, l;
	char *input_filename;
	char *output_filename;
} config_t;

/* 2D point structure */
typedef struct {
	char *acid;
} dna_t;

/* a point list structure */
typedef struct node_t {
	dna_t strand;
	struct node_t *next;
} node_t;

/* a [sum, count] cluster structure */
typedef struct {
	int **count;
} cluster_sum_t;

config_t *read_config();
dna_t* init_dataset(config_t *);
dna_t *init_centroid(dna_t *, int, int, int);
void kmeans(dna_t *, dna_t *, int, int, int);
void write_result(dna_t *, int, int, config_t *);
void print_result(dna_t *, int, int);
int compare_replace(dna_t *, dna_t *, int, int);
cluster_sum_t *compute_cluster_sum(node_t **, int, int);
node_t **compute_cluster(dna_t *, dna_t *, int, int, int);

#endif