#include "kmeans.h"

int main() {
	/* read config file information */
    config_t *config = read_config();
    /* get cluster number k */
    int k = config->k;
    /* initialize dataset into an array */
    point_t *dataset = init_dataset(config);
    /* get number of points */
    int num_of_points = config->count;
    /* compute initial centroids from dataset */
    point_t *centroids = init_centroid(dataset, k, num_of_points);
    /* start the kmeans process */
    kmeans(centroids, dataset, k, num_of_points);
    /* print the results */
    print_result(centroids, k);
    write_result(centroids, k, config);
    return 0;
}