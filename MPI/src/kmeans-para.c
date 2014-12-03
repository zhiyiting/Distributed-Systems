#include "mpi.h"
#include "kmeans.h"

/* MPI Code reference: https://computing.llnl.gov/tutorials/mpi/ */
int main(int argc, char **argv) {
	int num_tasks, rank, len, rc;
	MPI_Status Stat;
	char hostname[MPI_MAX_PROCESSOR_NAME];

	// construct data type
	MPI_Datatype point_type, cluster_type, oldtypes[2];
	int blockcounts[2];
	MPI_Aint offsets[2], extent;

	rc = MPI_Init(&argc, &argv);
	if (rc != MPI_SUCCESS) {
		printf ("Error starting MPI program. Terminating.\n");
		MPI_Abort(MPI_COMM_WORLD, rc);
	}

	MPI_Comm_size(MPI_COMM_WORLD, &num_tasks);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Get_processor_name(hostname, &len);
	printf ("Number of tasks= %d My rank= %d Running on %s\n", num_tasks, rank, hostname);

	offsets[0] = 0;
	oldtypes[0] = MPI_DOUBLE;
	blockcounts[0] = 2;

	MPI_Type_struct(1, blockcounts, offsets, oldtypes, &point_type);
	MPI_Type_commit(&point_type);

	offsets[0] = 0;
	oldtypes[0] = MPI_DOUBLE;
	blockcounts[0] = 2;
	MPI_Type_extent(MPI_DOUBLE, &extent);
	offsets[1] = 2 * extent;
	oldtypes[1] = MPI_INT;
	blockcounts[1] = 1;
	MPI_Type_struct(2, blockcounts, offsets, oldtypes, &cluster_type);
	MPI_Type_commit(&cluster_type);

	point_t *dataset;
	point_t *centroids;
	int k, num_of_points;
	int startflag = 1;

	if (rank == 0) {
		config_t *config = read_config();
    	k = config->k;
    	dataset = init_dataset(config);
    	num_of_points = config->count;
    	centroids = init_centroid(dataset, k, num_of_points);
    	cluster_sum_t *cluster_sum = (cluster_sum_t *)malloc(k * sizeof(cluster_sum_t));
    	int points_per_machine = num_of_points / (num_tasks - 1);
    	int remaining_points = num_of_points % (num_tasks - 1);
    	int points_sent = 0;
    	// send dataset to slaves
    	int remaining_point = num_of_points;
    	for (int i = 1; i < num_tasks; i++) {
    		int send_count = points_per_machine;
    		if (i == num_tasks - 1) {
    			send_count += remaining_points;
    		}
    		printf("Master: Send dataset to slave %d\n", i);
    		rc = MPI_Send(&send_count, 1, MPI_INT, i, 1, MPI_COMM_WORLD);
    		rc = MPI_Send(&dataset[points_sent], send_count, point_type, i, 2, MPI_COMM_WORLD);
    		rc = MPI_Send(&k, 1, MPI_INT, i, 3, MPI_COMM_WORLD);
    		points_sent += send_count;
    	}
    	point_t *new_centroids = (point_t *)malloc(k * sizeof(point_t));
    	cluster_sum_t *temp = (cluster_sum_t *)malloc(k * sizeof(cluster_sum_t));
    	do {
    		for (int i = 0; i < k; i++) {
    			cluster_sum[i].sum_x = 0.0;
     			cluster_sum[i].sum_y = 0.0;
     			cluster_sum[i].count = 0;
     		}
    		for (int i = 1; i < num_tasks; i++) {
    			rc = MPI_Send(&startflag, 1, MPI_INT, i, 0, MPI_COMM_WORLD);
    			printf("Master: Send centroids to slave %d\n", i);
    			rc = MPI_Send(centroids, k, point_type, i, 4, MPI_COMM_WORLD);
    			printf("Master: Receive result from slave %d\n", i);
    		}
    		for (int i = 1; i < num_tasks; i++) {
    			rc = MPI_Recv(temp, k, cluster_type, i, 5, MPI_COMM_WORLD, &Stat);
    			for (int j = 0; j < k; j++) {
    				cluster_sum[j].sum_x += temp[j].sum_x;
    				cluster_sum[j].sum_y += temp[j].sum_y;
    				cluster_sum[j].count += temp[j].count;
    			}
    		}
    		for (int i = 0; i < k; i++) {
        		point_t centroid;
        		cluster_sum_t ccluster = cluster_sum[i];
        		centroid.x = ccluster.sum_x / (double) ccluster.count;
        		centroid.y = ccluster.sum_y / (double) ccluster.count;
        		new_centroids[i] = centroid;
    		}
    	} while (!compare_replace(centroids, new_centroids, k));
    	startflag = 0;
    	for (int i = 1; i < num_tasks; i++) {
    		rc = MPI_Send(&startflag, 1, MPI_INT, i, 0, MPI_COMM_WORLD);
    	}
    	free(temp);
    	free(new_centroids);
    	free(cluster_sum);
    	print_result(centroids, k);
    	write_result(centroids, k, config);
	}
	else {
		rc = MPI_Recv(&num_of_points, 1, MPI_INT, 0, 1, MPI_COMM_WORLD, &Stat);
		printf("Slave %d: Received num of data points, which is %d\n", rank, num_of_points);
		dataset = (point_t *)malloc(num_of_points * sizeof(point_t));
		rc = MPI_Recv(dataset, num_of_points, point_type, 0, 2, MPI_COMM_WORLD, &Stat);
		printf("Slave %d: Received dataset\n", rank);
		rc = MPI_Recv(&k, 1, MPI_INT, 0, 3, MPI_COMM_WORLD, &Stat);
		rc = MPI_Recv(&startflag, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, &Stat);

		centroids = (point_t *)malloc(k * sizeof(point_t));
		while (startflag) {
			rc = MPI_Recv(centroids, k, point_type, 0, 4, MPI_COMM_WORLD, &Stat);
			printf("Slave %d: Received centroids\n", rank);
			node_t **cluster = compute_cluster(dataset, centroids, k, num_of_points);
			cluster_sum_t *cluster_sum = compute_cluster_sum(cluster, k);
			printf("Slave %d: Result computed\n", rank);
			rc = MPI_Send(cluster_sum, k, cluster_type, 0, 5, MPI_COMM_WORLD);
			printf("Slave %d: Send result to master\n", rank);
			free(cluster_sum);
			free(cluster);
			rc = MPI_Recv(&startflag, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, &Stat);
		}
		free(centroids);
		free(dataset);
		printf("finished\n");
	}
	free(point_type);
	free(cluster_type);
	MPI_Finalize();
	return 0;
}