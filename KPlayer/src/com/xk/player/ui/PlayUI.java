package com.xk.player.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.xk.player.core.BasicController;
import com.xk.player.core.BasicPlayer;
import com.xk.player.core.BasicPlayerEvent;
import com.xk.player.core.BasicPlayerException;
import com.xk.player.core.BasicPlayerListener;
import com.xk.player.lrc.LyricFrame;
import com.xk.player.tools.Config;
import com.xk.player.tools.FileUtils;
import com.xk.player.tools.Loginer;
import com.xk.player.tools.SWTResourceManager;
import com.xk.player.tools.SWTTools;
import com.xk.player.tools.SongLocation;
import com.xk.player.tools.SongSeacher;
import com.xk.player.tools.SongSeacher.SearchInfo;
import com.xk.player.uilib.ColorLabel;
import com.xk.player.uilib.Jindutiao;
import com.xk.player.uilib.ListItem;
import com.xk.player.uilib.MyList;
import com.xk.player.uilib.MyText;
import com.xk.player.uilib.MyText.DeleteListener;
import com.xk.player.uilib.listeners.DragEvent;
import com.xk.player.uilib.listeners.DragListener;
import com.xk.player.uilib.listeners.ItemEvent;
import com.xk.player.uilib.listeners.ItemListener;
import com.xk.player.uilib.listeners.ItemSelectionEvent;
import com.xk.player.uilib.listeners.ItemSelectionListener;

import org.eclipse.swt.widgets.Label;
import static com.xk.player.core.BasicPlayerEvent.*;

public class PlayUI implements BasicPlayerListener{

	protected Shell shell;
	private BasicPlayer player;
	private int currentPlay=0;
	private ColorLabel playButton;
	private Jindutiao jindutiao;
	private Jindutiao voice;
	private Map<String,Object> audioInfo;
	private MyList searchResult;;
	private MyList list;
	private MyList types;
	private int[]selections=new int[]{-1,-1};
	private ReentrantLock lock;
	private Condition cond;
	public long jumpedMillan=0;
	private boolean closed=false;
	private Label timeLabel;
	private Label lengthLabel;
	private Label songName;
	private long timeNow=0;
	private MyText text;
	private LyricFrame lFrame;
	private NormalWord lrcWord;
	private ColorLabel lrcword;
	private ColorLabel searchword;
	
	private int lrcModel=-1;//是否显示悬浮歌词
	
	private int pModel=0;//播放模式，默认循序播放
	
	private long lrcOffset=0l;//歌词延迟，前进
	
	private static PlayUI instance;
	
	public static PlayUI getInstance(){
		if(null==instance){
			instance=new PlayUI();
		}
		return instance;
	}
	
	private PlayUI(){
		
	}
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			PlayUI window = PlayUI.getInstance();
			Config.getInstance();
			window.open();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		lock=new ReentrantLock();
		cond=lock.newCondition();
		lFrame=new LyricFrame(this);
		lFrame.hide(false);
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		loop();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		Config config=Config.getInstance();
		shell = new Shell(SWT.NO_TRIM);
		Image source=config.BGTYPE==0?SWTResourceManager.getImage(PlayUI.class, config.BGPATH):SWTResourceManager.getImage(config.BGPATH);
		ImageData id=source.getImageData().scaledTo(1000, 666);
		Image back=new Image(null, id);
		shell.setBackgroundImage(back);
		shell.setImage(SWTResourceManager.getImage(PlayUI.class, "/images/jindutiao.png"));
		shell.setSize(1000, 666);
		shell.setText("Kplayer");
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		shell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				closed=true;
				Config.getInstance().save();
				SWTResourceManager.dispose();
				System.exit(0);
			}
		});
		
		SWTTools.enableTrag(shell);
		
		//歌曲名
		songName = new Label(shell, SWT.NONE);
		songName.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.NORMAL));
		songName.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		songName.setBounds(10, 10, 261, 17);
		songName.setText("KPlayer");
		
		Image skin=SWTResourceManager.getImage(PlayUI.class,"/images/skin.png");
		ColorLabel pifu=new ColorLabel(shell, SWT.NONE, skin, skin);
		pifu.setBounds(shell.getClientArea().width-28*3, 2, 19, 19);
		pifu.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				FileDialog dialog=new FileDialog(shell, SWT.NONE);
				dialog.setFilterExtensions(new String[]{"*.jpg","*.png"});
				dialog.setFilterNames(new String[]{"JPG图片","PNG图片"});
				dialog.setText("选择皮肤");
				String path=dialog.open();
				if(null!=path&&!path.isEmpty()){
					if(shell.getBackgroundImage()!=null){
						shell.getBackground().dispose();
					}
					Image img=SWTResourceManager.getImage(path);
					ImageData id=img.getImageData().scaledTo(1000, 666);
					Image back=new Image(null, id);
					shell.setBackgroundImage(back);
					Config.getInstance().BGTYPE=1;
					Config.getInstance().BGPATH=path;
				}
			}
		});
		
		//关闭按钮
		Image clo=SWTResourceManager.getImage(PlayUI.class,"/images/close.png");
		Image clofoc=SWTResourceManager.getImage(PlayUI.class,"/images/close_focus.png");
		ColorLabel close=new ColorLabel(shell, SWT.NONE, clo, clofoc);
		close.setBounds(shell.getClientArea().width-28, 0, 28, 19);
		close.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				shell.dispose();
			}
			
		});
		//最小化按钮
		Image min=SWTResourceManager.getImage(PlayUI.class,"/images/min.png");
		Image minfoc=SWTResourceManager.getImage(PlayUI.class,"/images/min_focus.png");
		ColorLabel m=new ColorLabel(shell, SWT.NONE, min, minfoc);
		m.setBounds(shell.getClientArea().width-28*2, 0, 28, 19);
		m.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseDown(MouseEvent arg0) {
				shell.setMinimized(true);
			}
			
		});
		
		//进度条
		jindutiao=new Jindutiao(shell, SWT.NONE, 370, 100);
		jindutiao.setBounds(0, 30, 0, 0);
		//当前播放进度
		timeLabel = new Label(shell, SWT.NONE);
		timeLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		timeLabel.setBounds(10, 49, 67, 17);
		timeLabel.setText("00:00");
		//歌曲时长
		lengthLabel = new Label(shell, SWT.RIGHT);
		lengthLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lengthLabel.setBounds(307, 49, 54, 17);
		lengthLabel.setText("00:00");
		
		//歌曲列表
		list=new MyList(shell, 315, 522);
		list.setLocation(46, 111);
		list.setMask(80);
		
		//分类列表
		types=new MyList(shell,64,522);
		types.setLocation(0,111);
		types.setSimpleSelect(true);
		
		Image music=SWTResourceManager.getImage(PlayUI.class, "/images/music.png");
		Image heart=SWTResourceManager.getImage(PlayUI.class, "/images/heart.png");
		TypeItem musics=new TypeItem(music);
		types.addItem(musics);
		TypeItem loved=new TypeItem(heart);
		types.addItem(loved);
		
		//显示歌词
		Image lrc=SWTResourceManager.getImage(PlayUI.class, "/images/lrc.png");
		ColorLabel lrcButton=new ColorLabel(shell,SWT.NONE,lrc,lrc);
		lrcButton.setBounds(16, 66, 25, 25);
		lrcButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				lrcModel*=-1;
				lFrame.hide(lrcModel!=-1);
			}
			
		});
		
		//播放模式
		final Image[]imgs=new Image[3];
		Image rand=SWTResourceManager.getImage(PlayUI.class, "/images/rand.png");
		Image goimg=SWTResourceManager.getImage(PlayUI.class, "/images/go.png");
		Image whileimg=SWTResourceManager.getImage(PlayUI.class, "/images/while.png");
		imgs[0]=goimg;
		imgs[1]=rand;
		imgs[2]=whileimg;
		final String[]tips =new String[]{"顺序播放","随机播放","单曲循环"};
		pModel=config.PLAY_MODEL;
		final ColorLabel playModel=new ColorLabel(shell,SWT.NONE,imgs[pModel],imgs[pModel]);
		playModel.setToolTipText(tips[pModel]);
		playModel.setBounds(50,64,32,32);
		playModel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				if(++pModel>2){
					pModel=0;
				}
				playModel.setToolTipText(tips[pModel]);
				playModel.setInner(imgs[pModel]);
				playModel.setFocus(imgs[pModel]);
				playModel.redraw();
				Config.getInstance().PLAY_MODEL=pModel;
			}
		});
		
		//播放控制按钮
		Image playfoc=SWTResourceManager.getImage(PlayUI.class, "/images/playbase.png");
		Image play=SWTResourceManager.getImage(PlayUI.class, "/images/playfoc.png");
		Image lastfoc=SWTResourceManager.getImage(PlayUI.class, "/images/lastbase.png");
		Image last=SWTResourceManager.getImage(PlayUI.class, "/images/lastfoc.png");
		Image nextfoc=SWTResourceManager.getImage(PlayUI.class, "/images/nextbase.png");
		Image next=SWTResourceManager.getImage(PlayUI.class, "/images/nextfoc.png");
		
		playButton = new ColorLabel(shell, SWT.NONE,play,playfoc);
		playButton.setBounds(155, 56, 46, 46);
		playButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				try {
					if(player.getStatus()==PLAYING){
						player.pause();
					}else if(player.getStatus()==PAUSED){
						player.resume();
					}else{
						lock.lock();
						try {
							cond.signalAll();
						} catch (Exception e) {
							return;
						}finally{
							lock.unlock();
						}
						
					}
				} catch (BasicPlayerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		
		ColorLabel lastButton = new ColorLabel(shell, SWT.NONE,last,lastfoc);
		lastButton.setBounds(100, 60, 36, 36);
		
		ColorLabel nextButton = new ColorLabel(shell, SWT.NONE,next,nextfoc);
		nextButton.setBounds(220, 60, 36, 36);
		nextButton.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				playNext();
			}
			
		});
		
		voice=new Jindutiao(shell, SWT.NONE, 100, config.maxVolume);
		voice.setCurrent(config.defaultVolume);
		voice.setBounds(270, 75, 0, 0);
		VoiceBody body=new VoiceBody();
		voice.setBody(body);
		voice.add(new DragListener() {
			
			@Override
			public void dragEnd(DragEvent e) {
				try {
					player.setGain(e.per);
					Config.getInstance().defaultVolume=(long) e.dragPoint;
				} catch (BasicPlayerException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}
		});
		
		ImageData heartData=heart.getImageData().scaledTo(20, 20);
		Image hImage=new Image(null, heartData);
		ColorLabel myLove = new ColorLabel(shell, SWT.NONE,hImage,hImage);
		myLove.setBounds(341, 12, 25, 25);
		myLove.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				int index=types.getSelectIndex();
				ListItem item=list.getSelection();
				if(index==0){
					SongItem it=(SongItem) item;
					String path=it.getProperty().get("path");
					if(!Config.getInstance().favoriteList.contains(path)){
						Config.getInstance().favoriteList.add(path);
					}
				}
			}
		});
		
		Image add1=SWTResourceManager.getImage(PlayUI.class, "/images/plus-sign.png");
		Image add2=SWTResourceManager.getImage(PlayUI.class, "/images/plus-dir.png");
		
		ColorLabel addSingle = new ColorLabel(shell, SWT.NONE, add1, add1);
		addSingle.setBounds(84, 635, 25, 25);
		addSingle.setToolTipText("添加歌曲");
		addSingle.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				FileDialog fd=new FileDialog(shell,SWT.NONE);
				fd.setFilterExtensions(new String[]{"*.mp3","*.ape"});
				fd.setFilterNames(new String[]{"MP3音乐文件","APE高品质音乐"});
				fd.setText("添加音乐");
				String path=fd.open();
				if(null!=path&&!path.isEmpty()){
					addFile(path,true);
				}
				list.flush();
			}
		});
		
		ColorLabel addDir = new ColorLabel(shell, SWT.NONE, add2, add2);
		addDir.setBounds(285, 635, 25, 25);
		addDir.setToolTipText("添加目录");
		addDir.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				DirectoryDialog dd=new DirectoryDialog(shell,SWT.NONE);
				dd.setText("添加目录");
				dd.setMessage("请选择目录");
				String dir=dd.open();
				if(null!=dir&&!dir.isEmpty()){
					File fl=new File(dir);
					if(fl.exists()&&fl.isDirectory()){
						String[]mp3s=fl.list(new FilenameFilter() {
							
							@Override
							public boolean accept(File dir, String name) {
								return name.toLowerCase().endsWith(".mp3")||name.toLowerCase().endsWith(".ape");
							}
						});
						for(String path:mp3s){
							addFile(dir+File.separator+path,true);
						}
						list.flush();
					}
					
				}
				
			}
		});
		
		
		//歌词控制
		Image goback=SWTResourceManager.getImage(PlayUI.class, "/images/goback.png");
		Image forward=SWTResourceManager.getImage(PlayUI.class, "/images/forward.png");
		Image setting=SWTResourceManager.getImage(PlayUI.class, "/images/setting.png");
		
		ColorLabel gobackLabel = new ColorLabel(shell, SWT.NONE, goback, goback);
		gobackLabel.setBounds(955, 270, 25, 25);
		gobackLabel.setToolTipText("后退");
		gobackLabel.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				lrcOffset-=500;
			}
		});
		
		ColorLabel settingLabel = new ColorLabel(shell, SWT.NONE, setting, setting);
		settingLabel.setBounds(955, 310, 25, 25);
		settingLabel.setToolTipText("设置");
		
		ColorLabel forwardLabel = new ColorLabel(shell, SWT.NONE, forward, forward);
		forwardLabel.setBounds(955, 350, 25, 25);
		forwardLabel.setToolTipText("前进");
		forwardLabel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				lrcOffset+=500;
			}
		});
		
		//歌词，搜索
		Image lrcpic=SWTResourceManager.getImage(PlayUI.class, "/images/lrcpic.png");
		lrcword = new ColorLabel(shell, SWT.NONE, lrcpic, lrcpic);
		lrcword.setBounds(545, 80, 70, 40);
		lrcword.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				showLrcView(true);
			}
		});
		
		Image searchpic=SWTResourceManager.getImage(PlayUI.class, "/images/searchpic.png");
		Image searchnor=SWTResourceManager.getImage(PlayUI.class, "/images/searchnor.png");
		searchword = new ColorLabel(shell, SWT.NONE, searchpic, searchnor);
		searchword.setBounds(645, 80, 70, 40);
		searchword.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				showLrcView(false);
			}
		});
		
		lrcWord = new NormalWord(shell, this);
		lrcWord.setBounds(470, 140, 455, 450);
		
		searchResult=new MyList(shell, 455, 450);
		searchResult.setBounds(470, 140, 455, 450);
		searchResult.setSimpleSelect(true);
		searchResult.setVisible(false);
		searchResult.setMask(180);
		SearchInfo head=new SearchInfo();
		head.album="专辑";
		head.name="歌名";
		head.singer="歌手";
		LTableItem it=new SongSearchItem(head);
		it.setHead(true);
		searchResult.addItem(it);
		
		
		Image search=SWTResourceManager.getImage(PlayUI.class, "/images/search.png");
		
		text = new MyText(shell, SWT.BORDER|SWT.SINGLE);
		text.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.NORMAL));
		text.setBounds(506, 10, 254, 35);
		text.setInnerImage( search);
		text.setNoTrim();
		text.addDeleteListener(new DeleteListener() {
			
			@Override
			public void deleteClicked() {
				processSearch();
				
			}
		});
		text.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode==SWT.CR){
					processSearch();
				}
			}
			
		});
		
		jindutiao.add(new DragListener() {
			
			@Override
			public void dragEnd(DragEvent e) {
				if(player.getStatus()==PLAYING){
					processSeek(e.per);
				}
				
			}
		});
		
		types.add(new ItemSelectionListener() {
			
			@Override
			public void selected(ItemSelectionEvent ev) {
				int index=types.getSelectIndex();
				list.clearAll();
				if(index==0){
					for(String path:Config.getInstance().songList){
						addFile(path,true);
					}
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							list.flush();
						}
					});
				}else{
					for(String path:Config.getInstance().favoriteList){
						addFile(path,true);
					}
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							list.flush();
						}
					});
				}
				
			}
		});
		
		list.addItemListener(new ItemListener() {
			
			@Override
			public void itemRemove(ItemEvent e) {
				SongItem item=(SongItem) e.item;
				removeFile(item.getProperty().get("path"));
				
			}
		});
		
		list.add(new ItemSelectionListener() {
			
			@Override
			public void selected(ItemSelectionEvent ev) {
				processItemSelection(ev);
				
			}
		});
		types.select(0, false);
		createPlayer();
	}
	
	private void showLrcView(boolean show){
		Image lrcpic=SWTResourceManager.getImage(PlayUI.class, "/images/lrcpic.png");
		Image lrcnor=SWTResourceManager.getImage(PlayUI.class, "/images/lrcnor.png");
		Image searchpic=SWTResourceManager.getImage(PlayUI.class, "/images/searchpic.png");
		Image searchnor=SWTResourceManager.getImage(PlayUI.class, "/images/searchnor.png");
		lrcWord.setVisible(show);
		searchResult.setVisible(!show);
		lrcword.setInner(show?lrcpic:lrcnor);
		searchword.setInner(show?searchpic:searchnor);
		lrcword.redraw();
		searchword.redraw();
	}
	
	private void processSearch(){
		String name=text.getText();
		if(!name.isEmpty()){
			showLrcView(false);
			List<SearchInfo> result=SongSeacher.getSongFromKuwo(name);
			SearchInfo head=new SearchInfo();
			head.album="专辑";
			head.name="歌名";
			head.singer="歌手";
			result.add(0, head);
			searchResult.clearAll();
			for(int i=0;i<result.size();i++){
				LTableItem item=new SongSearchItem(result.get(i));
				if(i==0){
					item.setHead(true);
				}
				searchResult.addItem(item);
				searchResult.flush();
			}
		}
	}
	
	
	private void processItemSelection(ItemSelectionEvent ev) {
		currentPlay=types.getSelectIndex();
		final SongItem item=(SongItem) ev.item;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				songName.setText(item.getProperty().get("name"));
			}
		});
		selections[types.getSelectIndex()]=list.getSelectIndex();
		lock.lock();
		try {
			cond.signalAll();
		} catch (Exception e) {
			return;
		}finally{
			lock.unlock();
		}
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String name=item.getProperty().get("artist");
				System.out.println("name:"+name);
				if(null!=name&&!name.isEmpty()){
					String path=SongSeacher.getArtistFromKuwo(name);
					System.out.println("path:"+path);
					if(null!=path&&!path.isEmpty()){
						SongLocation loc=Loginer.getInstance("player").getInputStream(path);
						if(null!=loc){
							ImageData[] img=new ImageLoader().load(loc.input);
							if(null!=img&&img.length>0){
								Image head=new Image(null,img[0].scaledTo(50, 50));
								item.setHead(head);
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										item.getParent().flush();
									}
								});
							}
						}
						
					}
					
					
				}
				
				
			}
		}).start();
		
	}
	
	private void removeFile(String path){
		int tps=types.getSelectIndex();
		if(tps==0){
			Config.getInstance().songList.remove(path);
		}else{
			Config.getInstance().favoriteList.remove(path);
		}
	}
	
	public void addFile(String path,boolean sync){
		int tps=types.getSelectIndex();
		if(tps==0){
			if(!Config.getInstance().songList.contains(path)){
				Config.getInstance().songList.add(path);
			}
		}else{
			if(!Config.getInstance().favoriteList.contains(path)){
				Config.getInstance().favoriteList.add(path);
			}
		}
		File file=new File(path);
		if(file.exists()&&file.isFile()){
			Config config=Config.getInstance();
			Map<String,String>pro=config.maps.get(path);
			try {
				if(null==pro){
					System.out.println("no properties found:"+path);
					pro=new HashMap<String, String>();
					MusicMetadataSet data = new MyID3().read(file);
					MusicMetadata metadata = (MusicMetadata) data.getSimplified();
					
					String artist=metadata.getArtist();
					String album=metadata.getAlbum();
					String title=metadata.getSongTitle();
					if(null!=artist){
						artist=new String(artist.getBytes(FileUtils.getEncoding(artist)),"GB2312");
						if(artist.indexOf("&")>=0){
							artist=artist.split("&")[0];
						}
					}else{
						artist=file.getName().split("-")[0].trim();
					}
					if(null!=album){
						album=new String(album.getBytes(FileUtils.getEncoding(album)),"GB2312");
					}
					if(null!=title){
						title=new String(title.getBytes(FileUtils.getEncoding(title)),"GB2312");
					}
					
					pro.put("artist", artist);
					pro.put("album", album);
					pro.put("title", title);
					config.maps.put(path, pro);
				}
				
			} catch (Exception e) {
				if(null==pro){
					System.out.println("no properties found:"+path);
					pro=new HashMap<String, String>();
				}
				String artist=file.getName().split("-")[0].trim();
				pro.put("artist", artist);
				config.maps.put(path, pro);
			}
			String name=file.getName();
			pro.put("name", name.substring(0, name.lastIndexOf(".")));
			pro.put("path", path);
			SongItem item=new SongItem(pro);
			list.addItem(item);
			if(list.getItemCount()-1==selections[tps]){
				item.select();
			}
			if(!sync){
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						list.flush();
					}
				});
				
			}
		}
	}

	private void loop(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(!closed){
					playMusic();
				}
				
			}
		}).start();
	}
	
	private void playNext(){
		if(currentPlay!=types.getSelectIndex()){
			types.select(currentPlay, true);
		}
		int now=list.getSelectIndex();
		int size=list.getItemCount();
		if(pModel==0){
			if(++now>=size){
				now=0;
			}
		}else if(pModel==1){
			now= new Random().nextInt(size);
		}
		
		list.select(now,true);
	}
	
	private void playMusic(){
		lock.lock();
		try {
			cond.await();
		} catch (InterruptedException e) {
			return;
		}finally{
			lock.unlock();
		}
		ListItem focus=list.getFocus();
		ListItem selected=list.getSelection();
		if(null==selected&&null!=focus){
			list.select(focus, true);
			selected=list.getSelection(); 
		}
		String path="";
		if(null!=selected){
			SongItem item=(SongItem) selected;
			path=item.getProperty().get("path");
			File file=new File(path);
			if(file.exists()&&file.isFile()){
				int status=player.getStatus();
				if(status==PLAYING||status==PAUSED){
					try {
						player.stop();
					} catch (BasicPlayerException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				try {
					player.open(file);
					player.play();
				} catch (Exception e1) {
					playNext();
				}
			}
		}
		
	}
	
	protected void processSeek(double rate) {
        try {
            if ((audioInfo != null) && (audioInfo.containsKey("audio.type"))) {
                String type = (String) audioInfo.get("audio.type");
                if ((type.equalsIgnoreCase("mp3")) && (audioInfo.containsKey("audio.length.bytes"))) {
                    long skipBytes = Math.round(((Integer) audioInfo.get("audio.length.bytes")).intValue() * rate);
                    player.seek(skipBytes);
                    double all=jindutiao.getAll();
                    jumpedMillan=(long) (all*rate);
                }else if ((type.equalsIgnoreCase("wave")) && (audioInfo.containsKey("audio.length.bytes"))) {
                    long skipBytes = Math.round(((Integer) audioInfo.get("audio.length.bytes")).intValue() * rate);
                    player.seek(skipBytes);
                    double all=jindutiao.getAll();
                    jumpedMillan=(long) (all*rate);
                } 
            } 
        } catch (BasicPlayerException ioe) {
        }
    }
	
	private void createPlayer(){
		player=new BasicPlayer();
		player.addBasicPlayerListener(this);
		player.addBasicPlayerListener(lrcWord);
		player.addBasicPlayerListener(lFrame.getLy());
	}
	
	public long getLrcOffset() {
		return lrcOffset;
	}

	public void setLrcOffset(long lrcOffset) {
		this.lrcOffset = lrcOffset;
	}
	
	/**
	 * 打开音频同时需要进行一些变量初始化
	 */
	@Override
	public void opened(Object stream, Map<String,Object> properties) {
		lrcOffset=0;
		jumpedMillan=0;
		timeNow=0;
		audioInfo=properties;
		final long all="Monkey's Audio (ape)".equals(properties.get("audio.type"))?(long) properties.get("duration"):(long) properties.get("duration")/1000;
		jindutiao.setAll( (all));
		jindutiao.setPersent(0d,true);
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				lengthLabel.setText(long2Seconds(all));
				timeLabel.setText("00:00");
				SongItem itm=(SongItem) list.getSelection();
				if(null!=itm){
					itm.put("all", long2Seconds(all));
				}
			}
		});
	}

	@Override
	public void progress(int bytesread, long microseconds, byte[] pcmdata, Map<String,Object> properties) {
		final long realTime=jumpedMillan+microseconds/1000;
		double per=realTime/(double)jindutiao.getAll();
		jindutiao.setPersent(per,true);
		if(realTime-timeNow>=1000){
			timeNow=realTime;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					timeLabel.setText(long2Seconds(realTime));
					SongItem itm=(SongItem) list.getSelection();
					if(null!=itm){
						itm.put("now", long2Seconds(realTime));
						list.flush();
					}
					
				}
			});
		}
		
		
	}

	@Override
	public void stateUpdated(BasicPlayerEvent event) {
		if(event.getCode()==EOM){
			playNext();
			
		}else if(event.getCode()==PLAYING){
			try {
				player.setGain(voice.getPersent());
			} catch (BasicPlayerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					Image stopfoc=SWTResourceManager.getImage(PlayUI.class, "/images/pausebase.png");
					Image stop=SWTResourceManager.getImage(PlayUI.class, "/images/pausefoc.png");
					playButton.setInner(stop);
					playButton.setFocus(stopfoc);
					playButton.redraw();
				}
			});
		}else if(event.getCode()==PAUSED){
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					Image playfoc=SWTResourceManager.getImage(PlayUI.class, "/images/playbase.png");
					Image play=SWTResourceManager.getImage(PlayUI.class, "/images/playfoc.png");
					playButton.setInner(play);
					playButton.setFocus(playfoc);
					playButton.redraw();
				}
			});
		}else if(event.getCode()==RESUMED){
			
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					Image stopfoc=SWTResourceManager.getImage(PlayUI.class, "/images/pausebase.png");
					Image stop=SWTResourceManager.getImage(PlayUI.class, "/images/pausefoc.png");
					playButton.setInner(stop);
					playButton.setFocus(stopfoc);
					playButton.redraw();
				}
			});
		}
		
	}

	@Override
	public void setController(BasicController controller) {
	}
	
	private String long2Seconds(long time){
		long sec=time/1000;
		int m=(int) (sec/60);
		int second=(int) (sec%60);
		return (m<10?("0"+m):m)+":"+(second<10?"0"+second:second);
	}

}
