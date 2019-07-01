//package com.canyugan.svnkit;
//
//import java.io.File;
//
//import org.tmatesoft.svn.core.SVNDepth;
//import org.tmatesoft.svn.core.SVNException;
//import org.tmatesoft.svn.core.SVNURL;
//import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
//import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
//import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
//import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
//import org.tmatesoft.svn.core.wc.ISVNOptions;
//import org.tmatesoft.svn.core.wc.SVNClientManager;
//import org.tmatesoft.svn.core.wc.SVNCommitClient;
//import org.tmatesoft.svn.core.wc.SVNCopyClient;
//import org.tmatesoft.svn.core.wc.SVNCopySource;
//import org.tmatesoft.svn.core.wc.SVNRevision;
//import org.tmatesoft.svn.core.wc.SVNUpdateClient;
//import org.tmatesoft.svn.core.wc.SVNWCUtil;
//
//public class ModelOption 
//{
//    private SVNClientManager ourClientManager;
//    private SVNURL repositoryOptUrl;
//    private String userName;
//    private String passwd;
//    
//    public ModelOption(String userName,String passwd)
//    {
//        this.userName=userName;
//        this.passwd=passwd;
//    }
//    
//    private void setUpSVNClient(String userName,String passwd)
//    {
//    	//svn协议
//        //SVNRepositoryFactoryImpl.setup();
//        
//    	//http协议
//        DAVRepositoryFactory.setup();
//        
//        //file协议
//        //FSRepositoryFactory.setup();
//        
//        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
//        ourClientManager = SVNClientManager.
//        		newInstance((DefaultSVNOptions) options, userName, passwd);
//    }
//    
//    /**
//     * 上传模型
//     * @param dirPath
//     */
//    public void uploadMoel(String dirPath,String modelName)
//    {
//        setUpSVNClient(userName,passwd);
//        File impDir = new File(dirPath);
//        SVNCommitClient commitClient = ourClientManager.getCommitClient();
//        commitClient.setIgnoreExternals(false);
//        try {
//            repositoryOptUrl = SVNURL.parseURIEncoded(RepositoryInfo.buffUrl+modelName);
//            commitClient.doImport(impDir,
//                    repositoryOptUrl, "import operation!", null, true, true,
//                    SVNDepth.INFINITY);
//        } catch (SVNException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//    
//    /**
//     * 下载模型
//     */
//    public void downloadModel(String downloadModelName,String dirPath)
//    {
//        setUpSVNClient(userName,passwd);
//        File outDir=new File(dirPath+"/"+downloadModelName);
//        //outDir.mkdirs();//创建目录
//        SVNUpdateClient updateClient=ourClientManager.getUpdateClient();
//        updateClient.setIgnoreExternals(false);
//        
//        try {
//            repositoryOptUrl=SVNURL.parseURIEncoded(RepositoryInfo.storeUrl+downloadModelName);
//            updateClient.doExport(repositoryOptUrl, outDir, SVNRevision.HEAD, SVNRevision.HEAD, "downloadModel",true,true);
//        } catch (SVNException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//    
//    /**
//     * 删除模型
//     */
//    public void deleteModel(String deleteModelName)
//    {
//        setUpSVNClient(userName,passwd);
//        SVNCommitClient commitClient=ourClientManager.getCommitClient();
//        commitClient.setIgnoreExternals(false);
//        
//        try {
//            repositoryOptUrl=SVNURL.parseURIEncoded(RepositoryInfo.storeUrl+deleteModelName);
//            SVNURL deleteUrls[]=new SVNURL[1];
//            deleteUrls[0]=repositoryOptUrl;
//            commitClient.doDelete(deleteUrls, "delete model");
//        } catch (SVNException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        
//    }
//    
//    /**
//     * 移动模型
//     */
//    public void moveModel(String modelName)
//    {
//        setUpSVNClient(userName,passwd);
//        SVNCopyClient copyClient=ourClientManager.getCopyClient();
//        copyClient.setIgnoreExternals(false);
//        
//        try {
//            repositoryOptUrl=SVNURL.parseURIEncoded(RepositoryInfo.buffUrl+modelName);
//            SVNURL destUrl=SVNURL.parseURIEncoded(RepositoryInfo.storeUrl+modelName);
//            SVNCopySource[] copySources = new SVNCopySource[1];
//            copySources[0] = new SVNCopySource(SVNRevision.HEAD, SVNRevision.HEAD, repositoryOptUrl);
//            
//            copyClient.doCopy(copySources, destUrl, true, false, false, "move", null);
//        } catch (SVNException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        
//    }
//}
//class RepositoryInfo {
//    public static String storeUrl="http://10.13.30.22/svn/SVNRepository/Checked/";
//    public static String buffUrl="http://10.13.30.22/svn/SVNRepository/UnChecked/";
//    public static String sysInfoUrl="http://10.13.30.22/svn/SVNRepository/Log/";
//}