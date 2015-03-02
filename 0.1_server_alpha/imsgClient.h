


#ifndef IMSG_CLIENT 
#define IMSG_CLIENT


	class imsgClient{
		public:
			imsgClient();
			imsgClient(SOCKET s);
			char * IP;
			int port;
			SOCKET socket;
			bool active;
		private:
	};
#endif