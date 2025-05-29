export interface User {
  id: number;
  username: string;
}

export interface Picture {
  id: number;
  filename: string;
  createdAt: string;
}

export interface Comment {
  id: number;
  userId: number;
  postId: number;
  content: string;
  createdAt: string;
}

export interface Topic {
  id: number;
  content: string;
  activeDate: string;
}

export interface Post {
  id: number;
  user: User;
  picture: Picture;
  createdAt: Date;
  likes: number;
  comments: Comment[];
  topic: Topic;
  isLiked: boolean; 
}