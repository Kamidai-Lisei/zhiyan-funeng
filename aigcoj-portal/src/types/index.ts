export interface Page<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export interface UserVO {
  id: number | string
  userName?: string
  userAccount?: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
  createTime?: string
}

export interface LoginUserVO extends UserVO {}

export interface JudgeConfig {
  timeLimit?: number
  memoryLimit?: number
  stackLimit?: number
}

export interface Question {
  id: number | string
  title: string
  content: string
  tags?: string[]
  answer?: string
  submitNum?: number
  acceptedNum?: number
  judgeConfig?: JudgeConfig
  thumbNum?: number
  favourNum?: number
  userId?: number
  createTime?: string
  userVO?: UserVO
}

export interface JudgeInfo {
  message?: string
  memory?: number
  time?: number
  score?: number
  passed?: number
  total?: number
  detail?: string
}

export interface QuestionSubmit {
  id: number | string
  language: string
  code?: string
  judgeInfo?: JudgeInfo
  status?: number
  questionId: number | string
  userId?: number
  createTime?: string
  userVO?: UserVO
  questionVO?: Question
}

export interface Post {
  id: number | string
  title: string
  content: string
  tags?: string
  tagList?: string[]
  thumbNum?: number
  favourNum?: number
  userId?: number
  createTime?: string
  user?: UserVO
  hasThumb?: boolean
  hasFavour?: boolean
}

export interface Material {
  id: number | string
  title: string
  description?: string
  category: string
  subject?: string
  fileType: string
  fileUrl: string
  coverUrl?: string
  fileSize?: number
  downloadNum?: number
  favourNum?: number
  viewNum?: number
  tags?: string
  uploaderId?: number | string
  status?: number
  createTime?: string
}

export interface ResumeContent {
  name: string
  phone: string
  email: string
  city: string
  summary: string
  education: { school: string; major: string; degree: string; time: string; gpa: string }[]
  projects: { name: string; role: string; time: string; desc: string; tech: string }[]
  skills: string[]
  awards: string[]
}

export interface Resume {
  id?: number
  userId?: number
  title: string
  targetSchool?: string
  targetMajor?: string
  content: string
  score?: number
  template?: string
  createTime?: string
  updateTime?: string
}

export interface Checkin {
  id?: number
  userId?: number
  checkinDate?: string
  duration?: number
  content?: string
}

export interface StudyPlan {
  id?: number
  userId?: number
  targetDate?: number | string
  dailyMinutes?: number
  subjects?: string
  motto?: string
}

export interface StudyOverview {
  checkinDays: number
  totalMinutes: number
  streak: number
  submitCount: number
  acceptedCount: number
  wrongCount: number
  favourCount: number
  recentCheckins: string[]
}
