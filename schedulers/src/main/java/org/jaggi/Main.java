package org.jaggi;


import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudsimplus.schedulers.vm.VmSchedulerTimeShared;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import java.util.ArrayList;
import java.util.List;


public class Main {

    // simulation constants
    private static final int VM_COUNT = 2; // number of VMs
    private static final int CLOUDLET_COUNT = 10; // number of tasks

    // host machine specs
    private static final int HOST_CORES = 8; // 8 cores in total
    private static final long HOST_MIPS = 1000; // each core speed -> million inst. per second
    private static final long HOST_RAM = 8192; // host machine total ram 8192 MB -> 8 GB
    private static final long HOST_BW = 10000; // host machine bandwidth MB/s
    private static final long HOST_STORAGE = 1000000; // storage 1000000 -> 1 TB

    // each VM specs
    private static final int VM_PES = 2; // 2 cores
    private static final long VM_MIPS = 1000; // each core MIPS cap in VM
    private static final long VM_RAM = 1024;  // ram allocated per VM in MB
    private static final long VM_BW = 1000;   // bandwidth available in each VM
    private static final long VM_SIZE = 10000; // storage per VM 10GB -> 10000 MB


    // cloutlet constants
    private static final int CLOUDLET_PES = 2; // required core by each cloudlet task
    private static final long CLOUDLET_LENGTH = 10000; // total instructions in MI unit -> 10000 * 10^6
    private static final long CLOUDLET_FILE_SIZE = 300; // input file size in each cloudlet 300 MB
    private static final long CLOUDLET_OUTPUT_SIZE = 300; // output file size 300MB


    public static void main(String[] args) {
        System.out.println("starting the simulation .........!!!!!!!!!");

        // init main class {CloudSimPlus} object
        CloudSimPlus sim = new CloudSimPlus();

        // create a datacenter -> inside which host machine will rest
        Datacenter datacenter = createDatacenter(sim);

        // create broker { analogy -> broker submit tasks[cloudlets] to VMs}
        DatacenterBroker broker = new DatacenterBrokerSimple(sim);

        // create a list of VMs
        List<Vm> vmList = createVmList();

        // create a list of cloudlets { that is workload per task} generally in MIPS
        List<Cloudlet> cloudletList = createCloudletList();

        // submit cloudlet list to broker init above
        // broker must get vm list that is vm addresses and tasks details that is cloudlet list
        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);

        // call start() method on main class -> main environment
        sim.start();

        // after simulation print result using CloudletsTableBuilder
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        new CloudletsTableBuilder(finishedCloudlets).build();  // Prints formatted results

        // simulations clock data
        System.out.printf("%nsimulation done clock data -> : %.2f seconds%n", sim.clock());
    }

    // create datacenter -> 1 host -> 8 cores
    private static Datacenter createDatacenter(CloudSimPlus sim) {
        List<Host> hostList = new ArrayList<>(); // list of hosts

        // list of cores each host possess
        List<Pe> peList = new ArrayList<>();
        for (int i = 0; i < HOST_CORES; i++) {
            // Each PeSimple represents one CPU core with a given MIPS capacity
            peList.add(new PeSimple(HOST_MIPS));
        }

        // create a host machine with above constants
        Host host = new HostSimple(HOST_RAM, HOST_BW, HOST_STORAGE, peList)
                // VM Scheduler -> how cpu cores shared among VMs
                .setVmScheduler(new VmSchedulerTimeShared());

        hostList.add(host);

        // Create and return a Datacenter with this host
        // A DatacenterSimple automatically manages host resource allocation
        return new DatacenterSimple(sim, hostList);
    }

    // create VMs lol
    private static List<Vm> createVmList() {
        List<Vm> vmList = new ArrayList<>();

        for (int i = 0; i < VM_COUNT; i++) {
            // Create a VM with MIPS, number of cores (PEs), and resource specifications
            Vm vm = new VmSimple(VM_MIPS, VM_PES)
                    .setRam(VM_RAM)     // set VM ram
                    .setBw(VM_BW)       // set VM bandwidth
                    .setSize(VM_SIZE)   // set VM storage
                    // cloud scheduler -> how cloudlets use cores on VM
                    .setCloudletScheduler(new CloudletSchedulerTimeShared());

            vmList.add(vm);
        }

        return vmList;
    }

    // create cloudlets
    private static List<Cloudlet> createCloudletList() {
        List<Cloudlet> cloudletList = new ArrayList<>();

        for (int i = 0; i < CLOUDLET_COUNT; i++) {
            // each cloudlet -> different workload (length in MI)
            long length = CLOUDLET_LENGTH + (i * 1000);

            // Create a cloudlet (task) with specific length and required PEs (cores)
            Cloudlet cloudlet = new CloudletSimple(length, CLOUDLET_PES)
                    .setFileSize(CLOUDLET_FILE_SIZE)       // input file size
                    .setOutputSize(CLOUDLET_OUTPUT_SIZE);  // output file size

            cloudletList.add(cloudlet);
        }

        return cloudletList;
    }
}
