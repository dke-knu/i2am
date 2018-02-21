package knu.cs.dke.prog.util.filter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import knu.cs.dke.prog.DBProcess;
import knu.cs.dke.prog.util.Constant;
import knu.cs.dke.vo.TrainingResult;
import knu.cs.dke.vo.TwitterEvent;

public class BayesianFilter {
	public static ArrayList<TrainingResult> FilterWord = new ArrayList<TrainingResult>();
	static int spam,ham;
	//training 결과 생성
	public boolean training(ArrayList<TwitterEvent> checked, ArrayList<TwitterEvent> unchecked, int log_idx){
		ArrayList<TrainingResult> train_set = new ArrayList<TrainingResult>();
		DBProcess dbProcess = new DBProcess();
		int saved_spam = 0;
		int saved_ham = 0;
		spam = checked.size();
		ham = unchecked.size();
		int spam_wordCount = 0;
		int ham_wordCount = 0;
//		ham = Constant.TwitterTotal - spam;
		//DB에 기존 저장되어 있는 것이 있다면 불러옴
		ArrayList<TrainingResult> saved_set = dbProcess.getTrainedData(log_idx);
		if(!saved_set.isEmpty()){
			//저장 된 데이터 있음

			train_set = saved_set;
			String[] counts = dbProcess.getLog(log_idx, false).split("\\|");
			saved_spam = Integer.parseInt(counts[0]);
			saved_ham = Integer.parseInt(counts[1]);
			System.out.println("!!!!!!!!!!저장된 데이터 있음 "+saved_spam+" "+saved_ham);
		}
		//전체 문서 수 update
		spam += saved_spam;
		ham += saved_ham;
		System.out.println("11111111spam: "+spam+", ham: "+ham);
		//데이터 정제..?
		for(TwitterEvent twit : checked){
			twit.setUserName(StringReplace(twit.getUserName()));
			twit.setUserName(continueSpaceRemove(twit.getUserName()));
			twit.setUserName(twit.getUserName().toLowerCase());
			twit.setLang(StringReplace(twit.getLang()));
			twit.setLang(continueSpaceRemove(twit.getLang()));
			twit.setText(StringReplace(twit.getText()));
			twit.setText(continueSpaceRemove(twit.getText()));
			twit.setText(twit.getText().toLowerCase());
		}
		for(TwitterEvent twit : unchecked){
			twit.setUserName(StringReplace(twit.getUserName()));
			twit.setUserName(continueSpaceRemove(twit.getUserName()));
			twit.setUserName(twit.getUserName().toLowerCase());
			twit.setLang(StringReplace(twit.getLang()));
			twit.setLang(continueSpaceRemove(twit.getLang()));
			twit.setText(StringReplace(twit.getText()));
			twit.setText(continueSpaceRemove(twit.getText()));
			twit.setText(twit.getText().toLowerCase());
		}

		ArrayList<String> spam_words_list = new ArrayList<String>();
		ArrayList<String> ham_words_list = new ArrayList<String>();

		//split하여 저장
		if(!checked.isEmpty()){
			for(TwitterEvent twit : checked){
				String twit_str = twit.getUserName()+" "+twit.getLang()+" "+twit.getText();
				String[] words = twit_str.split(" ");

				for(int i=0; i<words.length;i++){
					boolean isExist = false;
					if(train_set.isEmpty()){
						train_set.add(new TrainingResult(words[i],2,0,0,0));
						spam_words_list.add(words[i]);
					}else{
						for(int j=0; j<train_set.size();j++){
							if(words[i].equals(train_set.get(j).getWord())){
								//단어가 이미 있음 --> count 증가
								train_set.get(j).setSpamCount(train_set.get(j).getSpamCount()+1);
								spam_words_list.add(words[i]);
								isExist = true;
								break;
							}
						}
						if(!isExist){
							//단어가 없음 --> 추가
							train_set.add(new TrainingResult(words[i],2,0,0,0));
							spam_words_list.add(words[i]);
						}
					}
				}
			}
		} else{
			if(spam == 0) spam = 1;
		}
		System.out.println("checked ok..!!!!");
		int count = 0;
		if(!unchecked.isEmpty()){
			for(TwitterEvent twit : unchecked){
				String twit_str = twit.getUserName()+" "+twit.getLang()+" "+twit.getText();
				String[] words = twit_str.split(" ");
//				System.out.println((count+1)+" loop"+words.length);
				for(int i=0; i<words.length;i++){
					boolean isExist = false;
					if(train_set.isEmpty()){
						train_set.add(new TrainingResult(words[i],0,2,0,0));
						ham_words_list.add(words[i]);
					}else{
						for(int j=0; j<train_set.size();j++){
							//						System.out.print(words[i]+" ");
							if(words[i].equals(train_set.get(j).getWord())){
								//단어가 이미 있음 --> count 증가
								train_set.get(j).setHamCount(train_set.get(j).getHamCount()+1);
								ham_words_list.add(words[i]);
								isExist = true;
								break;
							}
						}
						if(!isExist){
							//단어가 없음 --> 추가
							train_set.add(new TrainingResult(words[i],0,2,0,0));
							ham_words_list.add(words[i]);
						}
					}
				}
				count++;
			}
		}else{
			if(ham == 0) ham = 1;
		}
		//체크된 것들의 단어 리스트
		ArrayList<String> unique_spam_words = new ArrayList<String>(new HashSet<String>(spam_words_list));
		ArrayList<String> unique_ham_words = new ArrayList<String>(new HashSet<String>(ham_words_list));

		System.out.println("unchecked ok..!!!!");
		//count가 0인것 1로 바꿈 (확률계산 위해서)
		for(TrainingResult train : train_set){
			double ws = train.getSpamCount()/(double)spam;
			double wh = train.getHamCount()/(double)ham;
			train.setWs(ws);
			train.setWh(wh);
			if(train.getSpamCount() == 0){
				train.setSpamCount(1);
//				train.setWs(0.5);
				train.setWs(1/(double)(spam+ham));
			}else if(train.getHamCount() == 0){
				train.setHamCount(1);
				train.setWh(1/(double)(spam+ham));
//				train.setWh(0.5);
			}
					
		}
		//DB에 저장

		if(!dbProcess.saveTrainData(train_set,log_idx, spam,ham)){
			System.out.println("fail");
			return false;
		}else {
			FilterWord = train_set;
			System.out.println("success");
			return true;
		}
	}

	public static boolean filter(String userName, String language,String text){
		userName = StringReplace(userName);
		userName = continueSpaceRemove(userName);
		text = StringReplace(text);
		text = continueSpaceRemove(text);

		String[] titleWord = userName.split(" ");
		String[] contentWord = text.split(" ");
		ArrayList<String> mailWords = new ArrayList<String>();
		//		double words_spam = 1;
		//		double words_ham = 1;
		//바꿈 원래는 double 0.0
		BigDecimal words_spam = new BigDecimal(String.valueOf(0.0));
		BigDecimal words_ham = new BigDecimal(String.valueOf(0.0));

		mailWords.add(language);
		//단어들 중복 제거
		for(int i=0; i<titleWord.length;i++){
			mailWords.add(titleWord[i]);
		}
		for(int i=0; i<contentWord.length;i++){
			mailWords.add(contentWord[i]);
		}
		//		System.out.println("기존 크기: "+mailWords.size());
		HashSet hs = new HashSet(mailWords);
		mailWords.clear();
		mailWords = new ArrayList<String>(hs);
		int count1=0,count2=0;
		//		System.out.println("중복 제거: "+mailWords.size());
		BigDecimal s_plus = null, h_plus=null;

		//각각 확률계산
		for(int i=0; i<mailWords.size();i++){	//spam
			String compare = mailWords.get(i);
			boolean isExist = false;
			if(compare==null || compare==" ");
			else{
				for(int j=0; j<FilterWord.size();j++){
					if(compare.equals(FilterWord.get(j).getWord())){
						count1++;
						//						words_spam *= FilterWord.get(j).getWs();
						//비교 할 단어 수 많아 소숫점 아래로 계속 내려가 구분이 어려울 정도로 작게나옴 그러므로 로그사용
						//바꿈
						//						words_spam += Math.log10(FilterWord.get(j).getWs());
						//						words_ham += Math.log10(FilterWord.get(j).getWh());
//						if((FilterWord.get(j).getWs()==0)&&(FilterWord.get(j).getWh()!=0)){
//							h_plus = new BigDecimal(String.valueOf(Math.log10(FilterWord.get(j).getWh())));
//							words_ham = words_ham.add(h_plus);
//						}else if((FilterWord.get(j).getWs()!=0)&&(FilterWord.get(j).getWh()==0)){
//							s_plus = new BigDecimal(String.valueOf(Math.log10(FilterWord.get(j).getWs())));
//							words_spam = words_spam.add(s_plus) ;
//						}else{
							s_plus = new BigDecimal(String.valueOf(Math.log10(FilterWord.get(j).getWs())));
							h_plus = new BigDecimal(String.valueOf(Math.log10(FilterWord.get(j).getWh())));
							words_spam = words_spam.add(s_plus) ;
							words_ham = words_ham.add(h_plus);
//						}
						isExist = true;
						break;
					}	
				}
				if(!isExist) {
					//					words_spam *= 0.5; //없는 단어 0.5
					//					words_spam += Math.log10(0.5); //없는 단어 0.5
					//					words_ham += Math.log10(0.5); //없는 단어 0.5
					s_plus = new BigDecimal(String.valueOf(Math.log10(0.5)));
					words_spam = words_spam.add(s_plus);
					words_ham = words_ham.add(s_plus);
					count2++;
				}
			}
		}

//		System.out.println(words_spam);
//		System.out.println(words_ham);
		//계산
		BigDecimal tmp1 = new BigDecimal(String.valueOf((spam/(double)(spam+ham))));
		BigDecimal tmp2 = new BigDecimal(String.valueOf((ham/(double)(spam+ham))));
		//		words_ham*(ham/(float)(spam+ham));
		BigDecimal spam_words = words_spam.multiply(tmp1);
		BigDecimal ham_words = words_ham.multiply(tmp2);
//		System.out.println(" "+spam_words);
//		System.out.println(" "+ham_words);
		//		double spam_words1 = words_spam + Math.log10(spam/(double)(spam+ham));
		//		double ham_words1 = words_ham + Math.log10(ham/(double)(spam+ham));
		//		System.out.println("spam 확률: "+spam_words);
		//		System.out.println("ham 확률: "+ham_words);
		//		System.out.println("spam 확률(log): "+spam_words1);
		//		System.out.println("ham 확률(log): "+ham_words1);

//		System.out.println("total: "+Constant.TwitterTotal);
		//		if(spam_words <= ham_words) return false; 	//ham
		//		else {System.out.println("true~ ");return true;}							//spam
		if(spam_words.compareTo(ham_words) <= 0){
			return false; 	//spam<ham
		}else{
			return true;	//spam>ham
		}
	}


	//특수문자 제거 하기
	private static String StringReplace(String str){       
		String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
		str =str.replaceAll(match, " ");
		return str;
	}
	//연속 스페이스 제거
	private static String continueSpaceRemove(String str){
		String match2 = "\\s{2,}";
		str = str.replaceAll(match2, " ");
		return str;
	}

}
