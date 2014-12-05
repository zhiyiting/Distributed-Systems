#include "kmeans.h"

int main() {
	/* read config file information */
    config_t *config = read_config();
    /* get cluster number k */
    int k = config->k;
    /* get number of points */
    int num_of_points = config->count;
    /* get length of dna */
    int dna_length = config->l;
    /* initialize dataset into an array */
    dna_t *dataset = init_dataset(config);
    /* compute initial centroids from dataset */
    dna_t *centroids = init_centroid(dataset, dna_length, k, num_of_points);
    /* start the kmeans process */
    kmeans(centroids, dataset, dna_length, k, num_of_points);
    /* print the results */
    print_result(centroids, dna_length, k);
    write_result(centroids, dna_length, k, config);
    return 0;
}