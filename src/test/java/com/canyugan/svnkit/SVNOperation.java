//package com.canyugan.svnkit;
//
//import java.util.Collection;
//import java.util.Iterator;
//import org.tmatesoft.svn.core.SVNDirEntry;
//import org.tmatesoft.svn.core.SVNNodeKind;
//import org.tmatesoft.svn.core.SVNURL;
//import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
//import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
//import org.tmatesoft.svn.core.io.SVNRepository;
//import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
//import org.tmatesoft.svn.core.wc.SVNWCUtil;
//
//public class SVNOperation 
//{
//	private static ISVNAuthenticationManager authManager;
//	private static SVNURL svnurl;
//	private static SVNRepository repository;
//	private static String userName = "caorui";
//	private static String password = "1qaz@WSX";
//
//	public static void main(String[] args) throws Exception
//	{
//        //1.根据访问协议初始化工厂
//        DAVRepositoryFactory.setup();
//        //2.初始化仓库
//        String url = "http://182.180.197.136:8080/svn/AI_SMP_Doc";
//        SVNRepository svnRepository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
//        //3.创建一个访问的权限
//        String username = "caorui";
//        String password = "1qaz@WSX";
//        char[] pwd = password.toCharArray();
//        ISVNAuthenticationManager authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(username,pwd);
//        svnRepository.setAuthenticationManager(authenticationManager);
//        /*输出仓库的根目录和UUID*/
//        System.out.println("Repository Root:" + svnRepository.getRepositoryRoot(true));
//        System.out.println("Repository UUID:" + svnRepository.getRepositoryUUID(true));
//        /**
//         * 检验某个URL（可以是文件、目录）是否在仓库历史的修订版本中存在，参数：被检验的URL，修订版本，这里我们想要打印出目录树，所以要求必须是目录
//         * SVNNodeKind的枚举值有以下四种：
//         *  SVNNodeKind.NONE    这个node已经丢失（可能是已被删除）
//         *  SVNNodeKind.FILE    文件
//         *  SVNNodeKind.DIR     目录
//         *  SVNNodeKind.UNKNOW  未知，无法解析
//         * */
//        /*
//         *  被检验的URL，本例有两种等价的写法。
//         *  1.不是以"/"开头的是相对于仓库驱动目录的相对目录，即svnRepository的url，在本例中是：空字符串（url目录是：https://wlyfree-PC:8443/svn/svnkitRepository1/trunk）
//         *  2.以"/"开头的是相对于svnRepository root目录的相对目录，即svnRepository的rootUrl，在本例中是：/trunk（root目录是https://wlyfree-pc:8443/svn/svnkitRepository1）
//         */
//
//        String checkUrl = "";
//        //修订版本号，-1代表一个无效的修订版本号，代表必须是最新的修订版
//        long revisionNum = -1;
//        SVNNodeKind svnNodeKind = svnRepository.checkPath(checkUrl,revisionNum);
//        if(svnNodeKind == SVNNodeKind.NONE){
//            System.err.println("This is no entry at " + checkUrl);
//            System.exit(1);
//        }else if(svnNodeKind == SVNNodeKind.FILE){
//            System.err.println("The entry at '" + checkUrl + "' is a file while a directory was expected.");
//            System.exit(1);
//        }else{
//            System.err.println("SVNNodeKind的值：" + svnNodeKind);
//        }
//        //打印出目录树结构
//        listEntries(svnRepository,checkUrl);
//        //打印最新修订版的版本号
//        //System.err.println("最新修订版版本号：" + svnRepository.getLatestRevision());
//    }
//	
//    @SuppressWarnings("rawtypes")
//	private static void listEntries(SVNRepository svnRepository,String path) throws Exception
//    {
//        System.err.println("path:" + path);
//        Collection entry =  svnRepository.getDir(path, -1 ,null,(Collection)null);
//        Iterator iterator = entry.iterator();
//        while(iterator.hasNext())
//        {
//            SVNDirEntry svnDirEntry = (SVNDirEntry)iterator.next();
//            System.out.println("path:" + "/" + (path.equals("") ? "" : path + "/") + svnDirEntry.getName()); 
//            		            //",(author:" + svnDirEntry.getAuthor() + 
//            		            //",revision:" + svnDirEntry.getRevision() + 
//            		            //",date:" + svnDirEntry.getDate() + ")");
//            if(svnDirEntry.getKind() == SVNNodeKind.DIR)
//            {
//                String tempPath = (path.equals("") ? svnDirEntry.getName() : path + "/" + svnDirEntry.getName()) ;
//                listEntries(svnRepository,tempPath);
//            }
//        }
//    }
//	
////	static 
////	{
////		// step 1
////		DAVRepositoryFactory.setup();
////
////		try {
////			// 创建一个驱动。（基于工厂），svnkit中repository所有的URL都基于SVNURL类生成，
////			// 编码不是UTF-8的话，可以调用SVNURL的parseURIEncoded()方法。url可以是项目根目录、目录或文件
////
////			/*
////			 * ①.不是以"/"开头，相对于驱动的绑定位置，即Repository的目录
////			 * ②.以"/"开头，代表repository的根，相对于Repository的Root对应的目录
////			 */
////			// step 2
////			svnurl = SVNURL.parseURIDecoded("http://182.180.197.136:8080/svn/AI_SMP_Doc");
////			repository = SVNRepositoryFactory.create(svnurl, null);
////
////			/*
////			 * 不同类型的身份验证凭据：(要依据实际类型创建) Kind Class representation Field of usage
////			 * PASSWORD SVNPasswordAuthentication login:password authentication
////			 * (svn://, http://) SSH SVNSSHAuthentication In svn+ssh:// tunneled
////			 * connections SSL SVNSSLAuthentication In secure https://
////			 * connections USERNAME SVNUserNameAuthentication With file:///
////			 * protocol, on local machines
////			 * 
////			 * 代理：如果authentication manager提供了一个非空的proxy
////			 * manager，SVNKit将通过代理服务器代理管理目标服务器。 SSL：支持SSL管理
////			 * ISVNAuthenticationManager的默认实现： ①.BasicAuthenticationManager：
////			 * 1.无需磁盘授权存储。 2.无需提供SSL provider 3.无需服务器或者文件配置。 4.使用proxy、ssh
////			 * setting、user credentials provoded到类的构造器中，无需身份验证提供者。
////			 * 5.不缓存credentials 。 ②.DefaultSVNAuthenticationManager：
////			 * 1.可以使用磁盘一个授权存储于默认Subversion运行的配置或者指定的目录。可以在目录中缓存credentials。
////			 * 2.使用运行时的内存存储credentials。 3.可以使用用户提供的username/password认证用户。
////			 * 4.使用SSL、SSH、Proxy setting、服务器配置文件运行Subvesion。 强制认证方案：
////			 * 尽管Subversion的优势是你不需要重复验证直到服务器要求验证。有时它可能是有效有能力让svnkit立即验证用户不浪费时间。
////			 * ISVNAuthenticationManager接口提供了这个能力，它在SVNKit使用一个行为控制时返回一个标识。
////			 * 
////			 * HTTP认证的方案： ①.Basic ②.Digest ③.NTLM
////			 * Basic、Digest两种方案，你需要提供一个用户名和密码：
////			 * 
////			 * ISVNAuthenticationManager authManager = new
////			 * BasicAuthenticationManager( "login" , "password" );
////			 * NTLM方案，你需要提供一个主域名： ISVNAuthenticationManager authManager = new
////			 * BasicAuthenticationManager( "DOMAIN\\login" , "password" );
////			 */
////			//step 3
////			authManager = new BasicAuthenticationManager(userName, password);
////			repository.setAuthenticationManager(authManager);
////		} catch (SVNException e) {
////			e.printStackTrace();
////		}
////	}
//}
