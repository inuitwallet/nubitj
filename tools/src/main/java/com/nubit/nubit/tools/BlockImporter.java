package main.java.com.nubit.nubit.tools;

import com.woollysammoth.nubitj.core.*;
import com.woollysammoth.nubitj.params.MainNetParams;
import com.woollysammoth.nubitj.params.TestNet3Params;
import com.woollysammoth.nubitj.store.*;
import com.woollysammoth.nubitj.utils.BlockFileLoader;
import com.google.common.base.Preconditions;

import java.io.File;

/** Very thin wrapper around {@link com.woollysammoth.nubitj.utils.BlockFileLoader} */
public class BlockImporter {
    public static void main(String[] args) throws BlockStoreException, VerificationException, PrunedException {
        System.out.println("USAGE: BlockImporter (prod|test) (H2|Disk|MemFull|Mem|SPV) [blockStore]");
        System.out.println("       blockStore is required unless type is Mem or MemFull");
        System.out.println("       eg BlockImporter prod H2 /home/user/nubitj.h2store");
        System.out.println("       Does full verification if the store supports it");
        Preconditions.checkArgument(args.length == 2 || args.length == 3);
        
        NetworkParameters params;
        if (args[0].equals("test"))
            params = TestNet3Params.get();
        else
            params = MainNetParams.get();
        
        BlockStore store;
        if (args[1].equals("H2")) {
            Preconditions.checkArgument(args.length == 3);
            store = new H2FullPrunedBlockStore(params, args[2], 100);
        } else if (args[1].equals("MemFull")) {
            Preconditions.checkArgument(args.length == 2);
            store = new MemoryFullPrunedBlockStore(params, 100);
        } else if (args[1].equals("Mem")) {
            Preconditions.checkArgument(args.length == 2);
            store = new MemoryBlockStore(params);
        } else if (args[1].equals("SPV")) {
            Preconditions.checkArgument(args.length == 3);
            store = new SPVBlockStore(params, new File(args[2]));
        } else {
            System.err.println("Unknown store " + args[1]);
            return;
        }
        
        AbstractBlockChain chain = null;
        if (store instanceof FullPrunedBlockStore)
            chain = new FullPrunedBlockChain(params, (FullPrunedBlockStore) store);
        else
            chain = new BlockChain(params, store);
        
        BlockFileLoader loader = new BlockFileLoader(params, BlockFileLoader.getReferenceClientBlockFileList());
        
        for (Block block : loader)
            chain.add(block);
    }
}
